package gomoku_main;

import java.util.ArrayList;

public class CudaTree extends SearchTree{
	

	
	public CudaTree() {
		treeNodes = new ArrayList<SearchNode>();
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
			if (treeNodes.get(i).isExhausted()) {
				UCBScore = -2;
			} else if (treeNodes.get(i).getPlayouts() != 0) {
				if (treeNodes.get(i).isFinalNode()) {
					UCBScore = 0;
				} else {
					UCBScore = UCBSearchValue(totalPlayouts, tempBoard, i);
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
			CudaNode node = (CudaNode) treeNodes.get(bestIndex);
			node.playout(tempBoard, blocks, threads);
			totalPlayouts += blocks * threads;
		} else {
			CudaNode node = (CudaNode) treeNodes.get(bestIndex);
			node.traverseNode(tempBoard, blocks, threads);
		}
	}

}
