package gomoku_main;

import java.util.ArrayList;
import java.util.List;

public class SearchNode {

	private int move;
	private int color;
	private int playouts;
	private double winrate;
	private boolean exhausted;
	private ArrayList<SearchNode> children;

	public SearchNode(int move, int color) {
		this.move = move;
		this.color = color;
		playouts = 0;
		winrate = 0;
		children = new ArrayList<SearchNode>();
		this.exhausted = false;
	}

	public void createChildrenNodes(Board board) {
		List<SearchNode> tempNodes = new ArrayList<SearchNode>();
		for (int i = 0; i < board.getBoardArea(); i++) {
			if (board.isLegalMove(i)) {
				tempNodes.add(new SearchNode(i, board.getColorToPlay()));
			}
		}
		int size = tempNodes.size();
		for (int i = 0; i < size; i++) {
			int rand = (int) (Math.random() * tempNodes.size());
			children.add(tempNodes.remove(rand));
		}
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

	public double getWinRate() {
		return winrate;
	}

	public boolean isExhausted() {
		return exhausted;
	}

	public int playout(Board board) {
		int randMove;
		int formerPlayouts = playouts;
		playouts += 1;
		Board tempBoard = new Board();
		tempBoard.copyBoard(board);
		while (!tempBoard.hasWinner()) {
			randMove = (int) (Math.random() * tempBoard.getBoardArea());
			tempBoard.play(randMove);
		}
		int winner = tempBoard.getWinner();
		if (winner == color) {
			winrate = (formerPlayouts * winrate + 1) / playouts;
		} if (winner == -1) {
			// If the result is a tie.
			winrate = (formerPlayouts * winrate + 0.5) / playouts;
		}
		return winner;
	}

	public int traverseNode(Board board) {
		board.play(this.move);
		double bestScore = -1;
		int bestIndex = -1;
		int win;
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
				UCTScore = winRate
						+ SearchTree.UCTK
						* Math.sqrt(Math.log(this.playouts)
								/ children.get(i).getPlayouts());
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

		if (children.get(bestIndex).getPlayouts() == 0) {
			win = children.get(bestIndex).playout(board);
			if (this.color == win) {
				winrate = (playouts * winrate + 1) / (playouts + 1);
			} else if (win == -1) {
				winrate = (playouts * winrate + 0.5) / (playouts + 1);
			}
			this.playouts++;
			return win;
		} else {
			win = children.get(bestIndex).traverseNode(board);
			if (win == -2) {
				win = this.traverseNode(board);
			}
			if (this.color == win) {
				winrate = (playouts * winrate + 1) / (playouts + 1);
			} else if (win == -1) {
				winrate = (playouts * winrate + 0.5) / (playouts + 1);
			}
			this.playouts++;
			return win;
		}

	}

	// board.play(this.move);
	// int childColor, randMove, winner, index;
	// do {
	// randMove = (int)(Math.random() * board.getBoardArea());
	// } while(!board.isLegalMove(randMove));
	// playouts += 1;
	// if(color == Board.BLACK) {
	// childColor = Board.WHITE;
	// } else {
	// childColor = Board.BLACK;
	// }
	// index = indexOfNode(randMove);
	// if(index == -1) {
	// children.add(new SearchNode(randMove, childColor));
	// winner = children.get(children.size() - 1).playout(board);
	// } else {
	// winner = children.get(index).traverseNode(board);
	// }
	// if(winner == color) {
	// wins += 1;
	// }
	// return winner;

	public String toString(int height) {
		String s = "";
		for (int i = 0; i < height; i++){
			s += "   ";
		}
		s += "Move: " + Board.indexToString(this.getMove()) + " Color: "
				+ Board.colorToString(this.getColor()) + " Playouts: "
				+ this.getPlayouts() + " Winrate: " + this.getWinRate() + "\n";
		if (this.children.size() != 0){
			for (SearchNode child: children){
				if (child.getPlayouts() != 0){
					s += child.toString(height + 1);
				}
			}
		}
		return s;
	}

	public int indexOfNode(int move) {
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).getMove() == move) {
				return i;
			}
		}
		return -1;
	}
}
