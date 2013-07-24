package gomoku_main;

import java.util.ArrayList;
import java.util.List;

public class SearchNode {

	protected int move;
	protected int color;
	protected int playouts;
	protected double lastWin;
	protected double winrate;
	protected boolean finalNode;
	protected boolean exhausted;
	protected ArrayList<SearchNode> children;

	public SearchNode(int move, int color) {
		this.move = move;
		this.color = color;
		finalNode = false;
		playouts = 0;
		winrate = 0;
		children = new ArrayList<SearchNode>();
		this.exhausted = false;
		lastWin = 0;
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

	public double getLastWin() {
		return lastWin;
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

	public boolean isFinalNode() {
		return finalNode;
	}

	public int playout(Board board) {
		int randMove;
		int formerPlayouts = playouts;
		playouts += 1;
		Board tempBoard = new Board();
		tempBoard.copyBoard(board);
		tempBoard.play(move);
		int winner = tempBoard.getWinner();
		if (winner != Board.VACANT) {
			finalNode = true;
			if (winner == color) {
				winrate = 1.0;
			}
			if (winner == -1) {
				// If the result is a tie.
				winrate = 0.65;
			}
			return winner;
		}
		while (winner == Board.VACANT) {
			randMove = (int) (Math.random() * tempBoard.getBoardArea());
			tempBoard.play(randMove);
			winner = tempBoard.getWinner();
		}
		if (winner == color) {
			winrate = (formerPlayouts * winrate + 1.0) / (playouts * 1.0);
			lastWin = 1.0;
		} else if (winner == -1) {
			// If the result is a tie.
			winrate = (formerPlayouts * winrate + 0.65) / (playouts * 1.0);
			lastWin = 0.65;
		} else {
			winrate = (formerPlayouts * winrate + 0.0) / (playouts * 1.0);
			lastWin = 0.0;
		}
		return winner;
	}

	public void setFinalNode(boolean b) {
		finalNode = b;
	}

	public void setPlayouts(int value) {
		playouts = value;
	}

	public void setWinRate(double value) {
		winrate = value;
	}

	public int traverseNodeUCB(Board board, double UCT) {
		board.play(this.move);
		double bestScore = -1;
		int bestIndex = -1;
		int win;
		double UCBScore;
		if (this.playouts <= 1) {
			createChildrenNodes(board);
		}
		if (children.size() == 0) {
			this.exhausted = true;
			return -2;
		}
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).isExhausted()) {
				UCBScore = -2;
			} else if (children.get(i).getPlayouts() != 0) {
				if (children.get(i).isFinalNode()) {
					UCBScore = 0.0;
				} else {
					UCBScore = UCBSearchValue(playouts, board, i);
				}
			} else {
				UCBScore = 0.45 + Math.random() * 0.1;
			}
			if (UCBScore > bestScore) {
				bestScore = UCBScore;
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
				winrate = (playouts * winrate + 1.0) / (playouts + 1.0);
				lastWin = 1.0;
			} else if (win == -1) {
				winrate = (playouts * winrate + 0.65) / (playouts + 1.0);
				lastWin = 0.65;
			} else {
				winrate = (playouts * winrate + 0.0) / (playouts + 1.0);
				lastWin = 0.0;
			}
			this.playouts++;
			return win;
		} else {
			win = children.get(bestIndex).traverseNodeUCB(board, UCT);
			if (win == -2) {
				win = this.traverseNodeUCB(board, UCT);
			}
			if (this.color == win) {
				winrate = (playouts * winrate + 1.0) / (playouts + 1.0);
				lastWin = 1.0;
			} else if (win == -1) {
				winrate = (playouts * winrate + 0.65) / (playouts + 1.0);
				lastWin = 0.65;
			} else {
				winrate = (playouts * winrate + 0.0) / (playouts + 1.0);
				lastWin = 0.0;
			}
			this.playouts++;
			return win;
		}

	}

	public int traverseNodeUCT(Board board, double UCT) {
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
				if (children.get(i).isFinalNode()) {
					UCTScore = 0.0;
				} else {
					UCTScore = winRate
							+ UCT
							* Math.sqrt(Math.log(this.playouts)
									/ children.get(i).getPlayouts());
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

		if (children.get(bestIndex).getPlayouts() == 0) {
			win = children.get(bestIndex).playout(board);
			if (this.color == win) {
				winrate = (playouts * winrate + 1.0) / (playouts + 1.0);
				lastWin = 1.0;
			} else if (win == -1) {
				winrate = (playouts * winrate + 0.65) / (playouts + 1.0);
				lastWin = 0.65;
			} else {
				winrate = (playouts * winrate + 0.0) / (playouts + 1.0);
				lastWin = 0.0;
			}
			this.playouts++;
			return win;
		} else {
			win = children.get(bestIndex).traverseNodeUCT(board, UCT);
			if (win == -2) {
				win = this.traverseNodeUCT(board, UCT);
			}
			if (this.color == win) {
				winrate = (playouts * winrate + 1.0) / (playouts + 1.0);
				lastWin = 1.0;
			} else if (win == -1) {
				winrate = (playouts * winrate + 0.65) / (playouts + 1.0);
				lastWin = 0.65;
			} else {
				winrate = (playouts * winrate + 0.0) / (playouts + 1.0);
				lastWin = 0.0;
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
	
	protected double UCBSearchValue(int totalPlayouts,
			Board board, int childIdx) {
		// The variable names here are chosen for consistency with the tech
		// report
		double barX = children.get(childIdx).getWinRate();
		double logParentRunCount = Math.log(totalPlayouts);
		// In the paper, term1 is the mean of the SQUARES of the rewards; since
		// all rewards are 0 or 1 here, this is equivalent to the mean of the
		// rewards, i.e., the win rate.
		double term1 = barX;
		double term2 = -(barX * barX);
		double term3 = Math.sqrt(2 * logParentRunCount / children.get(childIdx).getPlayouts());
		double v = term1 + term2 + term3; // This equation is above Eq. 1
		assert v >= 0 : "Negative variability in UCT for move "
				+ children.get(childIdx).getMove() + ":\nNode: " + childIdx + "\nterm1: " + term1
				+ "\nterm2: " + term2 + "\nterm3: " + term3
				+ "\nPlayer's board:\n" + board;
		double factor1 = logParentRunCount / children.get(childIdx).getPlayouts();
		double factor2 = Math.min(0.25, v);
		double uncertainty = 0.4 * Math.sqrt(factor1 * factor2);
		return uncertainty + barX;
	}

	public String toString() {
		return toString(0);
	}

	public String toString(int height) {
		String s = "";
		for (int i = 0; i < height; i++) {
			s += "   ";
		}
		s += "Move: " + Board.indexToString(this.getMove()) + " Color: "
				+ Board.colorToString(this.getColor()) + " Playouts: "
				+ this.getPlayouts() + " Winrate: " + this.getWinRate();
		if (finalNode) {
			s += " Final Node.";
		}
		s += "\n";
		if (this.children.size() != 0) {
			for (SearchNode child : children) {
				if (child.getPlayouts() != 0) {
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
