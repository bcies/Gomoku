package gomoku_main;

public class WinHeuristic {

	private static int checkColor(Board board, int x, int y) {
		int color = board.getPoint(x, y);
		boolean win = false;
		win = true;
		int bestMove = -1;
		int vacancies = 0;
		int vacantIndex = -1;
		if ((x >= 4) && (y <= board.getBoardWidth() - 5)) {
			for (int i = 1; i < 5; i++) {
				if (board.getPoint(x - i, y + i) == Board.VACANT) {
					if (vacancies >= 1) {
						win = false;
						break;
					}
					vacancies += 1;
					vacantIndex = board.getBoardIndex(x - i, y + i);
				} else if (board.getPoint(x - i, y + i) != color) {
					win = false;
					break;
				}
			}
			if (win) {
				if(board.getColorToPlay() == color) {
					return vacantIndex;
				}
				bestMove = vacantIndex;
			}
		}
		win = true;
		vacancies = 0;
		vacantIndex = -1;
		if ((x <= board.getBoardWidth() - 5)
				&& (y <= board.getBoardWidth() - 5)) {
			for (int i = 1; i < 5; i++) {
				if (board.getPoint(x + i, y + i) == Board.VACANT) {
					if (vacancies >= 1) {
						win = false;
						break;
					}
					vacancies += 1;
					vacantIndex = board.getBoardIndex(x + i, y + i);
				} else if (board.getPoint(x + i, y + i) != color) {
					win = false;
					break;
				}
			}
			if (win) {
				if(board.getColorToPlay() == color) {
					return vacantIndex;
				}
				bestMove = vacantIndex;
			}
		}
		win = true;
		vacancies = 0;
		vacantIndex = -1;
		if (y <= board.getBoardWidth() - 5) {
			for (int i = 1; i < 5; i++) {
				if (board.getPoint(x, y + i) == Board.VACANT) {
					if (vacancies >= 1) {
						win = false;
						break;
					}
					vacancies += 1;
					vacantIndex = board.getBoardIndex(x, y + i);
				} else if (board.getPoint(x, y + i) != color) {
					win = false;
					break;
				}
			}
			if (win) {
				if(board.getColorToPlay() == color) {
					return vacantIndex;
				}
				bestMove = vacantIndex;
			}
		}
		win = true;
		vacancies = 0;
		vacantIndex = -1;
		if (x <= board.getBoardWidth() - 5) {
			for (int i = 1; i < 5; i++) {
				if (board.getPoint(x + i, y) == Board.VACANT) {
					if (vacancies >= 1) {
						win = false;
						break;
					}
					vacancies += 1;
					vacantIndex = board.getBoardIndex(x + i, y);
				} else if (board.getPoint(x + i, y) != color) {
					win = false;
					break;
				}
			}
			if (win) {
				if(board.getColorToPlay() == color) {
					return vacantIndex;
				}
				bestMove = vacantIndex;
			}
		}
		return bestMove;
	}

	private static int checkVacant(Board board, int x, int y) {
		int color = board.VACANT;
		int bestMove = -1;
		boolean win = true;
		if ((x >= 4) && (y <= board.getBoardWidth() - 5)) {
			color = board.getPoint(x - 1, y + 1);
			if (color != Board.VACANT) {
				for (int i = 2; i < 5; i++) {
					if (board.getPoint(x - i, y + i) == Board.VACANT)
						if (board.getPoint(x - i, y + i) != color) {
							win = false;
							break;
						}
				}
				if (win) {
					if(color == board.getColorToPlay()) {
						return board.getBoardIndex(x, y);						
					}
					bestMove = board.getBoardIndex(x, y);
				}
			}
		}
		win = true;
		if ((x <= board.getBoardWidth() - 5)
				&& (y <= board.getBoardWidth() - 5)) {
			color = board.getPoint(x + 1, y + 1);
			if (color != Board.VACANT) {
				for (int i = 2; i < 5; i++) {
					if (board.getPoint(x + i, y + i) != color) {
						win = false;
						break;
					}
				}
				if (win) {
					if(color == board.getColorToPlay()) {
						return board.getBoardIndex(x, y);						
					}
					bestMove = board.getBoardIndex(x, y);
				}
			}
		}
		win = true;
		if (y <= board.getBoardWidth() - 5) {
			color = board.getPoint(x, y + 1);
			if (color != Board.VACANT) {
				for (int i = 2; i < 5; i++) {
					if (board.getPoint(x, y + i) != color) {
						win = false;
						break;
					}
				}
				if (win) {
					if(color == board.getColorToPlay()) {
						return board.getBoardIndex(x, y);						
					}
					bestMove = board.getBoardIndex(x, y);
				}
			}
		}
		win = true;
		if (x <= board.getBoardWidth() - 5) {
			color = board.getPoint(x + 1, y);
			if (color != Board.VACANT) {
				for (int i = 2; i < 5; i++) {
					if (board.getPoint(x + i, y) != color) {
						win = false;
						break;
					}
				}
				if (win) {
					if(color == board.getColorToPlay()) {
						return board.getBoardIndex(x, y);						
					}
					bestMove = board.getBoardIndex(x, y);
				}
			}
		}
		return bestMove;
	}

	public static int getGoodMove(Board board) {
		int color;
		int goodMove;
		for (int x = 0; x < board.getBoardWidth(); x++) {
			for (int y = 0; y < board.getBoardWidth(); y++) {
				color = board.getPoint(x, y);
				if (color != Board.VACANT) {
					goodMove = checkColor(board, x, y);
					if (goodMove != -1) {
						return goodMove;
					}
				} else {
					goodMove = checkVacant(board, x, y);
					if (goodMove != -1) {
						return goodMove;
					}
				}
			}
		}
		return -1;
	}
}
