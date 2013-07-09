package gomoku_main;

import java.util.ArrayList;

public class SearchTree {
	
	ArrayList<SearchNode> treeNodes;

	public SearchTree() {
		treeNodes = new ArrayList<SearchNode>();
	}
	
	public void expandTree(Board board) {
		Board tempBoard = new Board();
		tempBoard.copyBoard(board);
		int move;
		do {			
			move = (int) (Math.random() * tempBoard.getBoardArea());
		} while(!tempBoard.isLegalMove(move));
		int index = indexOfNode(move);
		if(index == -1) {
			treeNodes.add(new SearchNode(move, tempBoard.getColorToPlay()));
			treeNodes.get(treeNodes.size() - 1).playout(tempBoard);
		} else {
			treeNodes.get(index).traverseNode(tempBoard);
		}
	}
	
	public ArrayList<SearchNode> getNodes() {
		return treeNodes;
	}
	
	public int indexOfNode(int move) {
		for(int i = 0; i < treeNodes.size(); i++) {
			if(treeNodes.get(i).getMove() == move) {
				return i;
			}
		}
		return -1;
	}
}
