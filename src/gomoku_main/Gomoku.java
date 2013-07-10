package gomoku_main;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Gomoku {

	private static Board board;

	public static void main(String args[]) {
		board = new Board();
		List<Double> turnTimes = new LinkedList<Double>();
		Player black = new Player(20000);
		Player white = new Player(2000);
		String command;
		StringTokenizer token;
		Scanner in = new Scanner(System.in);
		while (!board.hasWinner()) {
			// command = in.nextLine();
			command = "genmove";
			if (command.contains("showboard")) {
				System.out.println(board);
				switch (board.getColorToPlay()) {
				case Board.BLACK:
					System.out.print("Black");
					break;
				case Board.WHITE:
					System.out.print("White");
					break;
				}
				System.out.println("'s turn:");
			} else if (command.contains("play ")) {
				token = new StringTokenizer(command, " ");
				token.nextToken();
				if (!board.play(token.nextToken())) {
					System.out.println("Invalid move.\n");
				}
			} else if (command.contains("genmove")) {
				int playerMove;
				long startTime = System.nanoTime();
				if (board.getColorToPlay() == Board.BLACK) {
					playerMove = black.getBestMove(board,
							command.contains("showtree"));
				}
				else {
					playerMove = white.getBestMove(board,
							command.contains("showtree"));
				}
				double turnTime = (System.nanoTime() - startTime) / 1000000.0;
				turnTimes.add(turnTime);
				System.out.println("Player played at "
						+ board.indexToString(playerMove));
				System.out.println("Player took " + (int) turnTime + "ms");
				board.play(playerMove);
			} else if (command.contains("quit")) {
				System.exit(0);
			}
		}
		System.out.println(board);
		if (board.getWinner() == Board.BLACK) {
			System.out.println("Black Wins!");
		} else if (board.getWinner() == Board.WHITE) {
			System.out.println("White Wins!");
		} else {
			System.out.println("It's a tie");
		}
		double sum = 0;
		for (Double time : turnTimes) {
			sum += time;
		}
		System.out.println("Average time taken: "
				+ (int) (sum / turnTimes.size()) + "ms");
	}
}
