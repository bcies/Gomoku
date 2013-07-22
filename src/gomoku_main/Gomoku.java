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
		boolean autoGame = false;
		int win;
		List<Double> turnTimes = new LinkedList<Double>();
		List<Integer> blackTurnPlayouts = new LinkedList<Integer>();
		List<Integer> whiteTurnPlayouts = new LinkedList<Integer>();
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
				int turnPlayout = 0;
				long startTime = System.nanoTime();
				if (board.getColorToPlay() == Board.BLACK) {
					playerMove = black.getBestMove(board,
							command.contains("showtree"));
					turnPlayout = black.getPlayouts();
					blackTurnPlayouts.add(turnPlayout);
				} else {
					playerMove = white.getBestMove(board,
							command.contains("showtree"));
					turnPlayout = white.getPlayouts();
					whiteTurnPlayouts.add(turnPlayout);
				}
				double turnTime = (System.nanoTime() - startTime) / 1000000.0;
				turnTimes.add(turnTime);
				System.out.println("Player played at "
						+ Board.indexToString(playerMove));
				System.out.println("Player took " + (int) turnTime + "ms");
				board.play(playerMove);
				System.out.println("Player did " + turnPlayout + " playouts");
			} else if (command.contains("autogame")) {
				autoGame = true;
			} else if (command.contains("settime ")) {
				token = new StringTokenizer(command, " ");
				token.nextToken();
				double time = Double.parseDouble(token.nextToken());
				if (black.setTimePerMove(time) && white.setTimePerMove(time)) {
					System.out.println("Time per move set to " + time
							+ " seconds.");
				} else {
					System.out.println("Invalid time.");
				}
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
		double timeSum = 0;
		for (Double time : turnTimes) {
			timeSum += time;
		}
		double blackPlayoutSum = 0;
		double whitePlayoutSum = 0;
		for (Integer playouts : blackTurnPlayouts) {
			blackPlayoutSum += playouts;
		}
		for (Integer playouts : whiteTurnPlayouts) {
			whitePlayoutSum += playouts;
		}
		System.out.println("Average time taken: "
				+ (int) (timeSum / turnTimes.size()) + "ms");
		System.out.println("Average black playouts per turn: "
				+ (int) (blackPlayoutSum / blackTurnPlayouts.size()));
		System.out.println("Average white playouts per turn: "
				+ (int) (whitePlayoutSum / whiteTurnPlayouts.size()));
		return win;
	}

	public static void main(String args[]) {
		double seconds = 5;

		Player black = new Player(seconds, true, true, 2);
		black.setCuda(14, 512);
		Player white = new Player(seconds, true, false, 2);
		// runExperiment(seconds);
		runGame(black, white);
	}

	public static void runExperiment(double seconds) {
		Player black = new Player(seconds, true, true, 3);
		Player white = new Player(seconds, true, false, 3);
		int blacksum = 0;
		int firstties = 0;
		int win;
		for (int i = 0; i < 360; i++) {
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
		for (int i = 0; i < 360; i++) {
			win = runGame(black, white);
			if (win == Board.WHITE) {
				whitesum += 1;
			} else if (win == Board.VACANT) {
				secondties += 1;
			}
		}
		System.out.println("\n");
		System.out.println("First  360 games: black uses UCB");
		System.out.println("Black won " + blacksum + " games");
		System.out.println("Ties: " + firstties);
		System.out.println("\nSecond 360 games: white uses UCB");
		System.out.println("White won " + whitesum + " games");
		System.out.println("Ties: " + secondties);

		try {
			File file = new File("results.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			OutputStream outStream = new FileOutputStream(file);
			Writer out = new OutputStreamWriter(outStream);
			out.write("First 360 games: black uses UCB");
			out.write("\nBlack won " + blacksum + " games");
			out.write("\nTies: " + firstties);
			out.write("\nSecond 360 games: white uses UCB");
			out.write("\nWhite won " + whitesum + " games");
			out.write("\nTies: " + secondties);
			out.close();

		} catch (Exception e) {
		}

	}
}
