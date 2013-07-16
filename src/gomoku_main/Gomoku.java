package gomoku_main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Gomoku {

	private static Board board;

	public static int runGame(Player black, Player white) {
		board = new Board();
		boolean autoGame = true;
		int win;
		List<Double> turnTimes = new LinkedList<Double>();
		String command;
		StringTokenizer token;
		Scanner in = new Scanner(System.in);
		while (!board.hasWinner()) {
			if (!autoGame) {
				command = in.nextLine();
			} else {
				command = "genmove";
			}
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
				} else {
					playerMove = white.getBestMove(board,
							command.contains("showtree"));
				}
				double turnTime = (System.nanoTime() - startTime) / 1000000.0;
				turnTimes.add(turnTime);
				System.out.println("Player played at "
						+ Board.indexToString(playerMove));
				System.out.println("Player took " + (int) turnTime + "ms");
				board.play(playerMove);
			} else if (command.contains("autogame")) {
				autoGame = true;
			} else if (command.contains("quit")) {
				System.exit(0);
			}
		}
		System.out.println(board);
		if (board.getWinner() == Board.BLACK) {
			System.out.println("Black Wins!");
			win = Board.BLACK;
		} else if (board.getWinner() == Board.WHITE) {
			System.out.println("White Wins!");
			win = Board.WHITE;
		} else {
			System.out.println("It's a tie");
			win = Board.VACANT;
		}
		double sum = 0;
		for (Double time : turnTimes) {
			sum += time;
		}
		System.out.println("Average time taken: "
				+ (int) (sum / turnTimes.size()) + "ms");
		return win;
	}

	public static void main(String args[]) {

		Player black = new Player(100000, true, true, 1);
		Player white = new Player(100000, true, true, 1);
		 runExperiment();
//		runGame(black, white);
	}

	public static void runExperiment() {
		Player black = new Player(20000, true, true, 3);
		Player white = new Player(20000, true, false, 3);
		int blacksum = 0;
		int firstties = 0;
		int win;
		for (int i = 0; i < 3520; i++) {
			win = runGame(black, white);
			if (win == Board.BLACK) {
				blacksum += 1;
			} else if (win == Board.VACANT) {
				firstties += 1;
			}
		}
		black = new Player(20000, true, false, 1);
		white = new Player(20000, true, true, 2);
		int whitesum = 0;
		int secondties = 0;
		for (int i = 0; i <3520; i++) {
			win = runGame(black, white);
			if (win == Board.WHITE) {
				whitesum += 1;
			} else if (win == Board.VACANT) {
				secondties += 1;
			}
		}
		System.out.println("\n");
		System.out.println("First  3520 games: black uses UCB");
		System.out.println("Black won " + blacksum + " games");
		System.out.println("Ties: " + firstties);
		System.out.println("\nSecond 3520 games: white uses UCB");
		System.out.println("White won " + whitesum + " games");
		System.out.println("Ties: " + secondties);

		try {
			File file = new File("results.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			OutputStream outStream = new FileOutputStream(file);
			Writer out = new OutputStreamWriter(outStream);
			out.write("First 3520 games: black uses UCB");
			out.write("Black won " + blacksum + " games");
			out.write("Ties: " + firstties);
			 out.write("\nSecond 3520 games: white uses UCB");
			 out.write("White won " + whitesum + " games");
			 out.write("Ties: " + secondties);
			out.close();

		} catch (Exception e) {
		}

	}
}
