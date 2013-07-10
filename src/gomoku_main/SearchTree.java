package gomoku_main;

import java.util.ArrayList;
import java.util.List;

public class SearchTree {

	ArrayList<SearchNode> treeNodes;
	private int totalPlayouts;
	public static final int UCTK = 1;

	public SearchTree() {
		treeNodes = new ArrayList<SearchNode>();
		totalPlayouts = 0;
	}

	public void createRootNodes(Board board) {
		List<SearchNode> tempNodes = new ArrayList<SearchNode>();
		for (int i = 0; i < board.getBoardArea(); i++) {
			if (board.isLegalMove(i)) {
				tempNodes.add(new SearchNode(i, board.getColorToPlay()));
			}
		}
		int size = tempNodes.size();
		for (int i = 0; i < size; i++) {
			int rand = (int) (Math.random() * tempNodes.size());
			treeNodes.add(tempNodes.remove(rand));
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
				double winRate = 1.0 * treeNodes.get(i).getWins()
						/ treeNodes.get(i).getPlayouts();
				UCTScore = winRate
						+ UCTK
						* Math.sqrt(Math.log(totalPlayouts)
								/ treeNodes.get(i).getPlayouts());
			} else {
				UCTScore = 0.2;
			}
			if (UCTScore > bestScore) {
				bestScore = UCTScore;
				bestIndex = i;
			}
		}

		if (treeNodes.get(bestIndex).getPlayouts() == 0) {
			treeNodes.get(bestIndex).playout(tempBoard);
		} else {
			treeNodes.get(bestIndex).traverseNode(tempBoard);
		}
		totalPlayouts++;
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
}
