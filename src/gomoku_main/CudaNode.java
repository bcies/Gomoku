package gomoku_main;

import java.util.ArrayList;
import java.util.List;
import static jcuda.driver.JCudaDriver.*;
import jcuda.*;
import jcuda.driver.*;
import jcuda.runtime.JCuda;

public class CudaNode extends SearchNode {

	protected ArrayList<CudaNode> children;

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
		int[] rand = new int[blocksxthreads];
		for (int i = 0; i < blocksxthreads; i++) {
			int randTemp = (int) (Math.random() * board.getBoardArea());
			if (board.isLegalMove(randTemp))
				rand[i] = randTemp;
		}

//		// first device
//		cuInit(0);
//		CUcontext pctx = new CUcontext();
//		CUdevice dev = new CUdevice();
//		cuDeviceGet(dev, 0);
//		cuCtxCreate(pctx, 0, dev);

		return -1;
	}

	public double traverseNode(Board board, int blocks, int threads) {
		board.play(this.move);
		double bestScore = -1;
		int bestIndex = -1;
		double wins;
		double UCTScore;
		if (this.playouts <= 1) {
			createChildrenNodes(board);
		}
		if (children.size() == 0) {
			this.exhausted = true;
			return -2;
		}
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).isExhausted()) {
				UCTScore = -2;
			} else if (children.get(i).getPlayouts() != 0) {
				double winRate = children.get(i).getWinRate();
				if (children.get(i).isFinalNode()) {
					UCTScore = 0.0;
				} else {
					UCTScore = winRate
							+ Math.sqrt((Math.log(playouts) / children.get(i)
									.getPlayouts())
									* Math.min(
											0.25,
											winRate
													- children.get(i)
															.getLastWin()
													+ Math.sqrt((2 * Math
															.log(playouts))
															/ children
																	.get(i)
																	.getPlayouts())));
				}
			} else {
				UCTScore = 0.5;
			}
			if (UCTScore > bestScore) {
				bestScore = UCTScore;
				bestIndex = i;
			}
		}
		if (bestIndex == -1) {
			this.exhausted = true;
			return -2;
		}
		int blocksxthreads = blocks * threads;
		if (children.get(bestIndex).getPlayouts() == 0) {
			wins = children.get(bestIndex).playout(board);
			int formerPlayouts = playouts;
			playouts += blocksxthreads;
			winrate = (formerPlayouts * winrate + wins) / (playouts);
			lastWin = wins / blocksxthreads;
			return blocksxthreads - wins;
		} else {
			wins = children.get(bestIndex).traverseNode(board, blocks, threads);
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
}
