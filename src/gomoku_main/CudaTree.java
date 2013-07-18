package gomoku_main;

import java.util.ArrayList;

public class CudaTree extends SearchTree{
	
	ArrayList<CudaNode> treeNodes;
	
	public CudaTree() {
		treeNodes = new ArrayList<CudaNode>();
		totalPlayouts = 0;
	}
	
	public void createRootNodes(Board board, boolean useHeuristics) {
		for (int i = 0; i < board.getBoardArea(); i++) {
			if (board.isLegalMove(i)) {
				treeNodes.add(new CudaNode(i, board.getColorToPlay()));
			}
		}
		if (useHeuristics) {
			applyHeuristic(board);
		}
	}
	
	public void expandTree(Board board, int blocks, int threads) {
		Board tempBoard = new Board();
		tempBoard.copyBoard(board);
		double bestScore = 0;
		int bestIndex = 0;
		double UCBScore;
		for (int i = 0; i < treeNodes.size(); i++) {
			if (treeNodes.get(i).getPlayouts() != 0) {
				double winRate = treeNodes.get(i).getWinRate();
				if (treeNodes.get(i).isFinalNode()) {
					UCBScore = 0;
				} else {
					UCBScore = winRate
							+ Math.sqrt(((Math.log(totalPlayouts)) / treeNodes
									.get(i).getPlayouts())
									* Math.min(
											0.25,
											treeNodes.get(i).getWinRate()
													- treeNodes.get(i)
															.getLastWin()
													+ Math.sqrt((2 * Math
															.log(totalPlayouts))
															/ treeNodes
																	.get(i)
																	.getPlayouts())));

				}
			} else {
				UCBScore = 0.45 + Math.random() * 0.1;
			}
			if (UCBScore > bestScore) {
				bestScore = UCBScore;
				bestIndex = i;
			}
		}
		if (treeNodes.get(bestIndex).getPlayouts() == 0) {
			treeNodes.get(bestIndex).playout(tempBoard, blocks, threads);
			totalPlayouts += blocks * threads;
		} else {
			treeNodes.get(bestIndex).traverseNode(tempBoard, blocks, threads);
		}
	}

}
