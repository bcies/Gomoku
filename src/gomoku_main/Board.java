package gomoku_main;

public class Board {

	public static final int VACANT = 0;
	public static final int BLACK = 1;
	public static final int WHITE = 2;

	private final int BOARD_WIDTH = 19;
	private int BOARD_AREA;
	private int[] board;
	private int colorToPlay;

	public Board() {
		BOARD_AREA = BOARD_WIDTH * BOARD_WIDTH;
		board = new int[BOARD_AREA];
		colorToPlay = BLACK;
	}
	
	public void copyBoard(Board fromBoard) {
		this.colorToPlay = fromBoard.getColorToPlay();
		int[] fromBoardBoard = fromBoard.getBoard();
		for(int i = 0; i < BOARD_AREA; i++) {
			this.board[i] = fromBoardBoard[i];
		}
	}

	public int[] getBoard() {
		return board;
	}

	public int getBoardIndex(int x, int y) {
		return x + y * BOARD_WIDTH;
	}

	public int getBoardWidth() {
		return BOARD_WIDTH;
	}
	
	public int getBoardArea() {
		return BOARD_AREA;
	}

	public int getColorToPlay() {
		return colorToPlay;
	}

	public int getWinner() {
		int color;
		boolean win = false;
		for (int x = 0; x < BOARD_WIDTH; x++) {
			for (int y = 0; y < BOARD_WIDTH; y++) {
				color = board[getBoardIndex(x, y)];
				if (color != VACANT) {
					win = true;
					if ((x >= 4) && (y <= 14)) {
						for (int i = 1; i < 5; i++) {
							if (board[getBoardIndex(x - i, y + i)] != color) {
								win = false;
								break;
							}
						}
						if (win) {
							return color;
						}
					}
					win = true;
					if ((x <= 14) && (y <= 14)) {
						for (int i = 1; i < 5; i++) {
							if (board[getBoardIndex(x + i, y + i)] != color) {
								win = false;
								break;
							}
						}
						if (win) {
							return color;
						}
					}
					win = true;
					if (y <= 14) {
						for (int i = 1; i < 5; i++) {
							if (board[getBoardIndex(x, y + i)] != color) {
								win = false;
								break;
							}
						}
						if (win) {
							return color;
						}
					}
					win = true;
					if (x <= 14) {
						for (int i = 1; i < 5; i++) {
							if (board[getBoardIndex(x + i, y)] != color) {
								win = false;
								break;
							}
						}
						if (win) {
							return color;
						}
					}
				}
			}
		}
		return VACANT;
	}

	public boolean hasWinner() {
		if (getWinner() == VACANT) {
			return false;
		} else {
			return true;
		}
	}
	
	public String indexToString(int index) {
		int row = (index / BOARD_WIDTH) + 1;
		int column = (index % BOARD_WIDTH);
		String string = "";
		if(column > 7) {
			column += 1;
		}
		string += (char)(column + 'a');
		string += row;
		return string;
	}

	public boolean isLegalMove(int index) {
		if ((index < 0) || (index > BOARD_AREA)) {
			return false;
		}
		if (board[index] != VACANT) {
			return false;
		}
		return true;
	}

	public void nextTurn() {
		if (colorToPlay == BLACK) {
			colorToPlay = WHITE;
		} else {
			colorToPlay = BLACK;
		}
	}

	public void placeStone(int index, int color) {
		board[index] = color;
	}

	public boolean play(String s) {
		if(s.length() >= 2) {			
			return play(stringToIndex(s));
		} else {
			return false;
		}
	}

	public boolean play(int index) {
		if (isLegalMove(index)) {
			placeStone(index, colorToPlay);
			nextTurn();
			return true;
		}
		return false;
	}

	public int stringToIndex(String string) {

		int column;
		if (string.charAt(0) < 96) {
			column = string.charAt(0) - 'A';
		} else {
			column = string.charAt(0) - 'a';
		}
		if (column == 8) {
			return -1;
		}
		if (column > 8) {
			column -= 1;
		}
		string = string.substring(1);
		int row = Integer.parseInt(string) - 1;
		int index = row * BOARD_WIDTH + column;
		return index;
	}

	@Override
	public String toString() {
		String s = "";
		for (int i = BOARD_WIDTH - 1; i >= 0; i--) {
			s += (i + 1) + " ";
			if (i <= 8) {
				s += " ";
			}
			for (int j = 0; j < BOARD_WIDTH; j++) {
				int color = board[i * BOARD_WIDTH + j];
				switch (color) {
				case VACANT:
					s += ". ";
					break;
				case BLACK:
					s += "# ";
					break;
				case WHITE:
					s += "O ";
					break;
				}
			}
			s += "\n";
		}
		s += "   A B C D E F G H J K L M N O P Q R S T\n";
		return s;
	}

}
