package gomoku_main;

import java.util.ArrayList;

public class SearchTree {

	ArrayList<SearchNode> treeNodes;
	private int totalPlayouts;
	public static final double UCTK = 0.1;

	public SearchTree() {
		treeNodes = new ArrayList<SearchNode>();
		totalPlayouts = 0;
	}
	
	public void applyHeuristic(Board board) {
		int winMove = WinHeuristic.getGoodMove(board);
		if(winMove != -1) {
			for(SearchNode n : treeNodes) {
				if(n.getMove() == winMove) {
					n.setWinRate(1.0);
					n.setPlayouts(1);
					n.setFinalNode(true);
					break;
				}
			}
		}
	}

	public void createRootNodes(Board board, boolean useHeuristics) {
		for (int i = 0; i < board.getBoardArea(); i++) {
			if (board.isLegalMove(i)) {
				treeNodes.add(new SearchNode(i, board.getColorToPlay()));
			}
		}
		if(useHeuristics) {			
			applyHeuristic(board);
		}
	}

	public void expandTree(Board board) {
		Board tempBoard = new Board();
		tempBoard.copyBoard(board);
		int move;
		do {
			move = (int) (Math.random() * tempBoard.getBoardArea());
		} while (!tempBoard.isLegalMove(move));
		int index = indexOfNode(move);
		if (index == -1) {
			treeNodes.add(new SearchNode(move, tempBoard.getColorToPlay()));
			treeNodes.get(treeNodes.size() - 1).playout(tempBoard);
		} else {
			treeNodes.get(index).traverseNode(tempBoard);
		}
	}

	public void expandUCTTree(Board board) {
		Board tempBoard = new Board();
		tempBoard.copyBoard(board);
		double bestScore = 0;
		int bestIndex = 0;
		double UCTScore;
		for (int i = 0; i < treeNodes.size(); i++) {
			if (treeNodes.get(i).getPlayouts() != 0) {
				double winRate = treeNodes.get(i).getWinRate();
				if (treeNodes.get(i).isFinalNode()) {
					UCTScore = 0;
				} else {
					UCTScore = winRate
							+ UCTK
							* Math.sqrt(Math.log(totalPlayouts)
									/ treeNodes.get(i).getPlayouts());

				}
			} else {
				UCTScore = 0.45 + Math.random() * 0.1;
			}
			if (UCTScore > bestScore) {
				bestScore = UCTScore;
				bestIndex = i;
			}
		}

		if (treeNodes.get(bestIndex).getPlayouts() == 0) {
			treeNodes.get(bestIndex).playout(tempBoard);
			totalPlayouts++;
		} else {
			treeNodes.get(bestIndex).traverseNode(tempBoard);
		}

	}

	public ArrayList<SearchNode> getNodes() {
		return treeNodes;
	}

	public int indexOfNode(int move) {
		for (int i = 0; i < treeNodes.size(); i++) {
			if (treeNodes.get(i).getMove() == move) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		String s = "";
		for (SearchNode node : treeNodes) {
			if (node.getPlayouts() != 0) {
				s += node.toString(0);

			}
		}
		return s;
	}
}
