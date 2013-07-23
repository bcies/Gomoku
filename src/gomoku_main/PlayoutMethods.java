package gomoku_main;

import static jcuda.driver.JCudaDriver.cuCtxSynchronize;
import static jcuda.driver.JCudaDriver.cuLaunchKernel;
import static jcuda.driver.JCudaDriver.cuMemAlloc;
import static jcuda.driver.JCudaDriver.cuMemcpyDtoH;
import static jcuda.driver.JCudaDriver.cuMemcpyHtoD;

import java.util.ArrayList;
import java.util.List;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;
import jcuda.runtime.JCuda;

public class PlayoutMethods {

	public static float[] playoutMultiLeaf(Board board, int blocks,
			int threads, int[] moves) {
		float wins[] = new float[moves.length];
		for (int i = 0; i < wins.length; i++) {
			wins[i] = 0;
		}

		int blocksxthreads = blocks * threads;
		Board tempBoard = new Board();
		// Generate all legal moves
		List<Integer> legalMoves = new ArrayList<Integer>();
		List<Integer> newLegalMoves = new ArrayList<Integer>();
		for (int i = 0; i < tempBoard.getBoardArea(); i++) {
			if (tempBoard.isLegalMove(i)) {
				legalMoves.add(i);
			}
		}
		// shuffle them
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
		cuMemcpyHtoD(d_board, Pointer.to(tempBoard.getBoard()), Sizeof.INT
				* board.getBoardArea());

		int boardWidth[] = { board.getBoardWidth() };
		CUdeviceptr d_boardWidth = new CUdeviceptr();
		cuMemAlloc(d_boardWidth, Sizeof.INT);
		cuMemcpyHtoD(d_boardWidth, Pointer.to(boardWidth), Sizeof.INT);

		int color[] = { board.getColorToPlay() };
		CUdeviceptr d_color = new CUdeviceptr();
		cuMemAlloc(d_color, Sizeof.INT);
		cuMemcpyHtoD(d_color, Pointer.to(color), Sizeof.INT);

		CUdeviceptr d_moves = new CUdeviceptr();
		cuMemAlloc(d_moves, Sizeof.INT * moves.length);
		cuMemcpyHtoD(d_moves, Pointer.to(moves), Sizeof.INT * moves.length);

		// Allocate device output memory
		CUdeviceptr d_wins = new CUdeviceptr();
		cuMemAlloc(d_wins, Sizeof.FLOAT * wins.length);
		cuMemcpyHtoD(d_wins, Pointer.to(wins), Sizeof.FLOAT * wins.length);

		// Set up the kernel parameters: A pointer to an array
		// of pointers which point to the actual values.
		Pointer kernelParameters = Pointer.to(Pointer.to(d_rand),
				Pointer.to(d_randNum), Pointer.to(d_board),
				Pointer.to(d_boardWidth), Pointer.to(d_color),
				Pointer.to(d_moves), Pointer.to(d_wins));

		// Call the kernel function.
		cuLaunchKernel(CudaNode.function, blocks, 1, 1, threads, 1, 1,
				0, null, kernelParameters, null);
		cuCtxSynchronize();

		cuMemcpyDtoH(Pointer.to(wins), d_wins, Sizeof.FLOAT*wins.length);

		JCuda.cudaFree(d_rand);
		JCuda.cudaFree(d_randNum);
		JCuda.cudaFree(d_board);
		JCuda.cudaFree(d_boardWidth);
		JCuda.cudaFree(d_color);
		JCuda.cudaFree(d_wins);
		return wins;
	}

}
