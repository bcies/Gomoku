package gomoku_main;

import java.util.ArrayList;

public class SearchTree {

	ArrayList<SearchNode> treeNodes;
	protected int totalPlayouts;
	public double UCTK = 0.1;

	public SearchTree() {
		treeNodes = new ArrayList<SearchNode>();
		totalPlayouts = 0;
	}

	public void applyHeuristic(Board board) {
		int winMove = WinHeuristic.getGoodMove(board);
		if (winMove != -1) {
			for (SearchNode n : treeNodes) {
				if (n.getMove() == winMove) {
					n.setWinRate(1.0);
					n.setPlayouts(1);
					n.setFinalNode(true);
					break;
				}
			}
		}
	}

	public void createRootNodes(Board board, boolean useHeuristics, double UCT) {
		for (int i = 0; i < board.getBoardArea(); i++) {
			if (board.isLegalMove(i)) {
				treeNodes.add(new SearchNode(i, board.getColorToPlay()));
			}
		}
		if (useHeuristics) {
			applyHeuristic(board);
		}
		UCTK = UCT;
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
			treeNodes.get(index).traverseNodeUCT(tempBoard, UCTK);
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
			treeNodes.get(bestIndex).traverseNodeUCT(tempBoard, UCTK);
		}

	}

	public void expandUCBTunedTree(Board board) {
		Board tempBoard = new Board();
		tempBoard.copyBoard(board);
		double bestScore = 0;
		int bestIndex = 0;
		double UCBScore;
		for (int i = 0; i < treeNodes.size(); i++) {
			if (treeNodes.get(i).getPlayouts() != 0) {
				if (treeNodes.get(i).isExhausted()) {
					UCBScore = -2;
				} else if (treeNodes.get(i).isFinalNode()) {
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
			treeNodes.get(bestIndex).playout(tempBoard);
			totalPlayouts++;
		} else {
			treeNodes.get(bestIndex).traverseNodeUCB(tempBoard, UCTK);
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

	protected double UCBSearchValue(int totalPlayouts,
			Board board, int childIdx) {
		// The variable names here are chosen for consistency with the tech
		// report
		double barX = treeNodes.get(childIdx).getWinRate();
		double logParentRunCount = Math.log(totalPlayouts);
		// In the paper, term1 is the mean of the SQUARES of the rewards; since
		// all rewards are 0 or 1 here, this is equivalent to the mean of the
		// rewards, i.e., the win rate.
		double term1 = barX;
		double term2 = -(barX * barX);
		double term3 = Math.sqrt(2 * logParentRunCount / treeNodes.get(childIdx).getPlayouts());
		double v = term1 + term2 + term3; // This equation is above Eq. 1
		assert v >= 0 : "Negative variability in UCT for move "
				+ treeNodes.get(childIdx).getMove() + ":\nNode: " + childIdx + "\nterm1: " + term1
				+ "\nterm2: " + term2 + "\nterm3: " + term3
				+ "\nPlayer's board:\n" + board;
		double factor1 = logParentRunCount / treeNodes.get(childIdx).getPlayouts();
		double factor2 = Math.min(0.25, v);
		double uncertainty = 0.4 * Math.sqrt(factor1 * factor2);
		return uncertainty + barX;
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
