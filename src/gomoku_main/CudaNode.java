package gomoku_main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import static jcuda.driver.JCudaDriver.*;
import jcuda.*;
import jcuda.driver.*;
import jcuda.runtime.JCuda;

public class CudaNode extends SearchNode {
	
	protected static CUfunction function;

	public static void prepareGPU() {
		// Note the following CUDA code came from
		// http://www.jcuda.org/samples/samples.html
		// Enable exceptions and omit all subsequent error checks
		JCudaDriver.setExceptionsEnabled(true);

		// Create the PTX file by calling the NVCC
		String ptxFileName = "";
		try {
			ptxFileName = preparePtxFile("/home/users/bcieslak/cuda-workspace/Gomoku/src/playout.cu");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Initialize the driver and create a context for the first device.
		cuInit(0);
		CUdevice device = new CUdevice();
		cuDeviceGet(device, 0);
		CUcontext context = new CUcontext();
		cuCtxCreate(context, 0, device);

		// Load the ptx file.
		CUmodule module = new CUmodule();
		cuModuleLoad(module, ptxFileName);

		// Obtain a function pointer to the "add" function.
		function = new CUfunction();
		cuModuleGetFunction(function, module, "playout");

	}

	public CudaNode(int move, int color) {
		super(move, color);
	}

	public void createChildrenNodes(Board board) {
		List<CudaNode> tempNodes = new ArrayList<CudaNode>();
		for (int i = 0; i < board.getBoardArea(); i++) {
			if (board.isLegalMove(i)) {
				tempNodes.add(new CudaNode(i, board.getColorToPlay()));
			}
		}
		int size = tempNodes.size();
		for (int i = 0; i < size; i++) {
			int rand = (int) (Math.random() * tempNodes.size());
			children.add(tempNodes.remove(rand));
		}
	}

	public double playout(Board board, int blocks, int threads) {
		int blocksxthreads = blocks * threads;
		Board tempBoard = new Board();
		tempBoard.copyBoard(board);
		tempBoard.play(move);
		int formerPlayouts = this.playouts;
		playouts += blocksxthreads;
		int winner = tempBoard.getWinner();
		if (winner != Board.VACANT) {
			finalNode = true;
			if (winner == color) {
				winrate = 1.0;
			}
			if (winner == -1) {
				// If the result is a tie.
				winrate = 0.5;
			}
			return blocksxthreads - winrate * blocksxthreads;
		}
		//Generate all legal moves
		List<Integer> legalMoves = new ArrayList<Integer>();
		List<Integer> newLegalMoves = new ArrayList<Integer>();
		for(int i = 0; i < tempBoard.getBoardArea(); i++) {
			if(tempBoard.isLegalMove(i)) {
				legalMoves.add(i);
			}
		}
		//shuffle them
		int size = legalMoves.size();
		int[] rand = new int[blocks * size];
		for (int j = 0; j < blocks; j++) {
			for (int i = 0; i < size; i++) {
				int randNumber = (int) (Math.random() * legalMoves.size());
				newLegalMoves.add(legalMoves.get(randNumber));
				rand[j * size + i] = legalMoves.remove(randNumber);
			}
			legalMoves.addAll(newLegalMoves);
			newLegalMoves.clear();
		}

		// Allocate the device input data, and copy the
		// host input data to the device
		CUdeviceptr d_rand = new CUdeviceptr();
		cuMemAlloc(d_rand, Sizeof.INT * rand.length);
		cuMemcpyHtoD(d_rand, Pointer.to(rand), Sizeof.INT * rand.length);

		int randNum[] = { rand.length };
		CUdeviceptr d_randNum = new CUdeviceptr();
		cuMemAlloc(d_randNum, Sizeof.INT);
		cuMemcpyHtoD(d_randNum, Pointer.to(randNum), Sizeof.INT);

		CUdeviceptr d_board = new CUdeviceptr();
		cuMemAlloc(d_board, Sizeof.INT * board.getBoardArea());
		cuMemcpyHtoD(d_board, Pointer.to(tempBoard.getBoard()),
				Sizeof.INT * board.getBoardArea());

		int boardWidth[] = { board.getBoardWidth() };
		CUdeviceptr d_boardWidth = new CUdeviceptr();
		cuMemAlloc(d_boardWidth, Sizeof.INT);
		cuMemcpyHtoD(d_boardWidth, Pointer.to(boardWidth), Sizeof.INT);

		int color[] = { board.getColorToPlay() };
		CUdeviceptr d_color = new CUdeviceptr();
		cuMemAlloc(d_color, Sizeof.INT);
		cuMemcpyHtoD(d_color, Pointer.to(color), Sizeof.INT);

		// Allocate device output memory
		float wins[] = { (float) 0.0 };
		CUdeviceptr d_wins = new CUdeviceptr();
		cuMemAlloc(d_wins, Sizeof.FLOAT);
		cuMemcpyHtoD(d_wins, Pointer.to(wins), Sizeof.FLOAT);

		// Set up the kernel parameters: A pointer to an array
		// of pointers which point to the actual values.
		Pointer kernelParameters = Pointer.to(Pointer.to(d_rand),
				Pointer.to(d_randNum), Pointer.to(d_board),
				Pointer.to(d_boardWidth), Pointer.to(d_color),
				Pointer.to(d_wins));

		// Call the kernel function.
		cuLaunchKernel(function, blocks, 1, 1, threads, 1, 1, 0, null,
				kernelParameters, null);
		cuCtxSynchronize();

		
		cuMemcpyDtoH(Pointer.to(wins), d_wins, Sizeof.FLOAT);

		JCuda.cudaFree(d_rand);
		JCuda.cudaFree(d_randNum);
		JCuda.cudaFree(d_board);
		JCuda.cudaFree(d_boardWidth);
		JCuda.cudaFree(d_color);
		JCuda.cudaFree(d_wins);
		winrate = (formerPlayouts * winrate + wins[0]) / (playouts * 1.0);
		lastWin = wins[0] / (threads * blocks);
		return wins[0];
	}

	/**
	 * The extension of the given file name is replaced with "ptx". If the file
	 * with the resulting name does not exist, it is compiled from the given
	 * file using NVCC. The name of the PTX file is returned.
	 * 
	 * source: http://www.jcuda.org/samples/samples.html
	 * 
	 * @param cuFileName
	 *            The name of the .CU file
	 * @return The name of the PTX file
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	private static String preparePtxFile(String cuFileName) throws IOException {
		int endIndex = cuFileName.lastIndexOf('.');
		if (endIndex == -1) {
			endIndex = cuFileName.length() - 1;
		}
		String ptxFileName = cuFileName.substring(0, endIndex + 1) + "ptx";
		File ptxFile = new File(ptxFileName);
		if (ptxFile.exists()) {
			return ptxFileName;
		}

		File cuFile = new File(cuFileName);
		if (!cuFile.exists()) {
			throw new IOException("Input file not found: " + cuFileName);
		}
		String modelString = "-m" + System.getProperty("sun.arch.data.model");
		String command = "nvcc -arch=sm_20 -v " + modelString + " -ptx "
				+ cuFile.getPath() + " -o " + ptxFileName;

		System.out.println("Executing\n" + command);
		Process process = Runtime.getRuntime().exec(command);

		String errorMessage = new String(toByteArray(process.getErrorStream()));
		String outputMessage = new String(toByteArray(process.getInputStream()));
		int exitValue = 0;
		try {
			exitValue = process.waitFor();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while waiting for nvcc output",
					e);
		}

		if (exitValue != 0) {
			System.out.println("nvcc process exitValue " + exitValue);
			System.out.println("errorMessage:\n" + errorMessage);
			System.out.println("outputMessage:\n" + outputMessage);
			throw new IOException("Could not create .ptx file: " + errorMessage);
		}

		System.out.println("Finished creating PTX file");
		return ptxFileName;
	}

	/**
	 * Fully reads the given InputStream and returns it as a byte array
	 * 
	 * source: http://www.jcuda.org/samples/samples.html
	 * 
	 * @param inputStream
	 *            The input stream to read
	 * @return The byte array containing the data from the input stream
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	private static byte[] toByteArray(InputStream inputStream)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte buffer[] = new byte[8192];
		while (true) {
			int read = inputStream.read(buffer);
			if (read == -1) {
				break;
			}
			baos.write(buffer, 0, read);
		}
		return baos.toByteArray();
	}

	public double traverseNode(Board board, int blocks, int threads) {
		board.play(this.move);
		double bestScore = -1;
		int bestIndex = -1;
		double wins;
		double UCBScore;
		int blocksxthreads = blocks * threads;
		if (this.playouts <= blocksxthreads) {
			createChildrenNodes(board);
		}
		if (children.size() == 0) {
			this.exhausted = true;
			return -2;
		}
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).isExhausted()) {
				UCBScore = -2;
			} else if (children.get(i).getPlayouts() != 0) {
				if (children.get(i).isFinalNode()) {
					UCBScore = 0.0;
				} else {
					UCBScore = UCBSearchValue(playouts, board, i);
				}
			} else {
				UCBScore = 0.45 + Math.random() * 0.1;
			}
			if (UCBScore > bestScore) {
				bestScore = UCBScore;
				bestIndex = i;
			}
		}
		if (bestIndex == -1) {
			this.exhausted = true;
			return -2;
		}
		if (children.get(bestIndex).getPlayouts() == 0) {
			CudaNode node = (CudaNode) children.get(bestIndex);
			wins = node.playout(board, blocks, threads);
			int formerPlayouts = playouts;
			playouts += blocksxthreads;
			winrate = (formerPlayouts * winrate + wins) / (playouts);
			lastWin = wins / blocksxthreads;
			return blocksxthreads - wins;
		} else {
			CudaNode node = (CudaNode) children.get(bestIndex);
			wins = node.traverseNode(board, blocks, threads);
			if (wins == -2) {
				wins = this.traverseNode(board, blocks, threads);
			}
			int formerPlayouts = playouts;
			playouts += blocksxthreads;
			winrate = (formerPlayouts * winrate + wins) / (playouts);
			lastWin = wins / blocksxthreads;
			return blocksxthreads - wins;
		}

	}
	
	public double traverseNodeMultiLeaf(Board board, int blocks, int threads) {
		board.play(this.move);
		double UCBScore;
		List<Double> sortedScore = new ArrayList<Double>();
		List<Integer> sortedIndex = new ArrayList<Integer>();
		int blocksxthreads = blocks * threads;
		if (this.playouts <= blocksxthreads) {
			createChildrenNodes(board);
		}
		if (children.size() == 0) {
			this.exhausted = true;
			return -2;
		}
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).isExhausted()) {
				UCBScore = -2;
			} else if (children.get(i).getPlayouts() != 0) {
				if (children.get(i).isFinalNode()) {
					UCBScore = 0.0;
				} else {
					UCBScore = UCBSearchValue(playouts, board, i);
				}
			} else {
				UCBScore = 0.45 + Math.random() * 0.1;
			}
			boolean flag = true;
			for (int j = 0; j < sortedScore.size(); j++){
				if (UCBScore > sortedScore.get(j)){
					sortedScore.add(j, UCBScore);
					sortedIndex.add(j, i);
					flag = false;
					break;
				}
			}
			if (flag){
				sortedScore.add(UCBScore);
				sortedIndex.add(i);
			}
		}
		if (sortedScore.get(0) <= -1) {
			this.exhausted = true;
			return -2;
		}
		if (children.get(sortedIndex.get(0)).getPlayouts() == 0) {
			
			int[] bestIndex = new int[blocks];
			int[] bestMove = new int[blocks];
			int count = 0;
			int i = 0;
			while (count < blocks){
				if (i < sortedScore.size()){
					if (children.get(sortedIndex.get(i)).getPlayouts() == 0){
						bestIndex[count] = sortedIndex.get(i);
						bestMove[count] = children.get(sortedIndex.get(i)).getMove();
						count++;
					}
				}
				else{
					bestIndex[count] = -1;
					bestMove[count] = -1;
					count++;
				}
				i++;
			}
			double[] wins = PlayoutMethods.playoutMultiLeaf(board, blocks, threads, bestMove);
			int formerPlayouts = playouts;
			playouts += blocksxthreads;
			for (i = 0; i < bestIndex.length; i++){
				if (bestIndex[i] != -1){
					double value = (children.get(bestIndex[i]).getWinRate()*formerPlayouts + wins[i])/playouts;
					children.get(bestIndex[i]).setWinRate(value);
					children.get(bestIndex[i]).setPlayouts(children.get(bestIndex[i]).getPlayouts() + threads);
				}
			}
			double sumWins = 0;
			for (i = 0; i < wins.length; i++){
				if (wins[i] != -1) sumWins += wins[i];
			}
			return blocksxthreads - sumWins;
		} else {
			CudaNode node = (CudaNode) children.get(sortedIndex.get(0));
			double win = node.traverseNodeMultiLeaf(board, blocks, threads);
			if (win == -2) {
				win = this.traverseNodeMultiLeaf(board, blocks, threads);
			}
			int formerPlayouts = playouts;
			playouts += blocksxthreads;
			winrate = (formerPlayouts * winrate + win) / (playouts);
			lastWin = win / blocksxthreads;
			return blocksxthreads - win;
		}

	}
}
