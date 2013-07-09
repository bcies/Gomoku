package gomoku_main;

import java.util.ArrayList;

public class SearchNode {
	
	private int move;
	private int color;
	private int playouts;
	private int wins;
	private ArrayList<SearchNode> children;

	public SearchNode(int move, int color) {
		this.move = move;
		this.color = color;
		playouts = 0;
		wins = 0;
		children = new ArrayList<SearchNode>();
	}
	
	public int getColor() {
		return color;
	}
	
	public int getMove() {
		return move;
	}
	
	public int getPlayouts() {
		return playouts;
	}
	
	public int getWins() {
		return wins;
	}
	
	public int playout(Board board) {
		int randMove;
		this.playouts += 1;
		Board tempBoard = new Board();
		tempBoard.copyBoard(board);
		while(!tempBoard.hasWinner()) {
			randMove = (int)(Math.random() * tempBoard.getBoardArea());
			tempBoard.play(randMove);
		}
		int winner = tempBoard.getWinner();
		if(winner == color) {
			wins += 1;
		}
		return winner;
	}

	public int traverseNode(Board board) {
		board.play(this.move);
		int childColor, randMove, winner, index;
		do {	
			randMove = (int)(Math.random() * board.getBoardArea()); 
		} while(!board.isLegalMove(randMove));
		playouts += 1;
		if(color == Board.BLACK) {
			childColor = Board.WHITE;
		} else {
			childColor = Board.BLACK;
		}
		index = indexOfNode(randMove);
		if(index == -1) {
			children.add(new SearchNode(randMove, childColor));
			winner = children.get(children.size() - 1).playout(board);
		} else {
			winner = children.get(index).traverseNode(board);
		}
		if(winner == color) {
			wins += 1;
		}
		return winner;
	}
	
	public int indexOfNode(int move) {
		for(int i = 0; i < children.size(); i++) {
			if(children.get(i).getMove() == move) {
				return i;
			}
		}
		return -1;
	}
}
