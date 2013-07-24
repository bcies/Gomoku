package gomoku_main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Gomoku {

	private static Board board;
	private static boolean autoGame = false;
	private static double seconds = 5.0;

	public static int runGame(Player black, Player white) {
		board = new Board();
		int win;
		List<Double> turnTimes = new LinkedList<Double>();
		List<Long> blackTurnPlayouts = new LinkedList<Long>();
		List<Long> whiteTurnPlayouts = new LinkedList<Long>();
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
				long turnPlayout = 0;
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
		for (Long playouts : blackTurnPlayouts) {
			blackPlayoutSum += playouts;
		}
		for (Long playouts : whiteTurnPlayouts) {
			whitePlayoutSum += playouts;
		}
		System.out.println("Average time taken: "
				+ (int) (timeSum / turnTimes.size()) + "ms");
		System.out.println("Average black playouts per turn: "
				+ (long) (blackPlayoutSum / blackTurnPlayouts.size()));
		System.out.println("Average white playouts per turn: "
				+ (long) (whitePlayoutSum / whiteTurnPlayouts.size()));
		return win;
	}

	public static void main(String args[]) {
		Player black = new Player(seconds, true, true, 1);
		Player white = new Player(seconds, true, true, 1);
		boolean startGame = false;
		Scanner in = new Scanner(System.in);
		System.out
				.println("Run Experiment or Play Game?\n(Type experiment or game)");
		while (!startGame) {
			String command = in.nextLine();
			if (command.contains("experiment")) {
				startGame = true;
				runExperiment();
			} else if (command.contains("game")) {
				startGame = true;
				runGame(black, white);
			}
		}
	}

	public static void runExperiment() {
		int totalGames;
		int blocks;
		int threads;
		int player1UCTK;
		int player2UCTK;
		double timePerMove;
		boolean player1CUDA;
		boolean player2CUDA;
		boolean player1Multi;
		boolean player2Multi;
		boolean player1Heuristics;
		boolean player2Heuristics;
		boolean player1UCB;
		boolean player2UCB;
		String settingsDescription;
		String resultsDirectory;

		// load a properties file

		Properties defaultProp = new Properties();
		try {
			defaultProp.load(new FileInputStream("default.properties"));
		} catch (FileNotFoundException e1) {
			System.err.println("config.properties not found.");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Properties userProp = new Properties(defaultProp);
		try {
			userProp.load(new FileInputStream("user.properties"));
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}

		/**Getting settings from .properties files. */
		
		autoGame = Boolean.parseBoolean(userProp.getProperty("autogame"));
		
		totalGames = Integer.parseInt(userProp.getProperty("totalgames"));
		
		timePerMove = Double.parseDouble(userProp.getProperty("timepermove"));
		
		blocks = Integer.parseInt(userProp.getProperty("blocks"));
		
		threads = Integer.parseInt(userProp.getProperty("threads"));
		
		player1CUDA = Boolean.parseBoolean(userProp.getProperty("blkuseCUDA"));
		
		player1Multi = Boolean.parseBoolean(userProp.getProperty("blkuseMultiLeaf"));
		
		player1Heuristics = Boolean.parseBoolean(userProp
				.getProperty("blkuseheuristics"));
		
		player1UCB = Boolean.parseBoolean(userProp.getProperty("blkuseUCB"));
		
		player1UCTK = Integer.parseInt(userProp.getProperty("blkUCTconstant"));
		
		player2CUDA = Boolean.parseBoolean(userProp.getProperty("whtuseCUDA"));
		
		player2Multi = Boolean.parseBoolean(userProp.getProperty("whtuseMultiLeaf"));
		
		player2Heuristics = Boolean.parseBoolean(userProp
				.getProperty("whtuseheuristics"));
		
		player2UCB = Boolean.parseBoolean(userProp.getProperty("whtuseUCB"));
		
		player2UCTK = Integer.parseInt(userProp.getProperty("whtUCTconstant"));
		
		settingsDescription = userProp.getProperty("settingsDescription");
		
		resultsDirectory = userProp.getProperty("resultsdirectory");

		
		seconds = timePerMove;

		Player black = new Player(seconds, player1Heuristics, player1UCB,
				player1UCTK);
		if(player1CUDA) {
			black.setCuda(blocks, threads, player1Multi);
		}
		Player white = new Player(seconds, player2Heuristics, player2UCB,
				player2UCTK);
		if(player2CUDA) {
			white.setCuda(blocks, threads, player2Multi);
		}
		int blacksum = 0;
		int firstties = 0;
		int win;
		for (int i = 0; i < (totalGames / 2); i++) {
			System.out.println("First half: Game number " + (i + 1));
			win = runGame(black, white);
			if (win == Board.BLACK) {
				blacksum += 1;
			} else if (win == Board.VACANT) {
				firstties += 1;
			}
		}
		black = new Player(seconds, player2Heuristics, player2UCB, player2UCTK);
		if(player2CUDA) {
			black.setCuda(blocks, threads, player1Multi);
		}
		white = new Player(seconds, player1Heuristics, player1UCB, player1UCTK);
		if(player1CUDA) {
			white.setCuda(blocks, threads, player2Multi);
		}
		int whitesum = 0;
		int secondties = 0;
		for (int i = 0; i < (totalGames / 2); i++) {
			System.out.println("Second half: Game number " + (i + 1));
			win = runGame(black, white);
			if (win == Board.WHITE) {
				whitesum += 1;
			} else if (win == Board.VACANT) {
				secondties += 1;
			}
		}
		System.out.println("\n");
		System.out.println("Settings:");
		System.out.println(settingsDescription);
		System.out.println("First " + (totalGames / 2) + " games:");
		System.out.println("Black won " + blacksum + " games");
		System.out.println("Ties: " + firstties);
		System.out.println("\nSecond " + (totalGames / 2) + " games:");
		System.out.println("White won " + whitesum + " games");
		System.out.println("Ties: " + secondties);

		try {
			File file = new File(resultsDirectory + "results.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			OutputStream outStream = new FileOutputStream(file);
			Writer out = new OutputStreamWriter(outStream);
			out.write("Settings:\n" + settingsDescription);
			out.write("\nFirst " + (totalGames / 2) + " games:");
			out.write("\nBlack won " + blacksum + " games");
			out.write("\nTies: " + firstties);
			out.write("\nSecond " + (totalGames / 2) + " games:");
			out.write("\nWhite won " + whitesum + " games");
			out.write("\nTies: " + secondties);
			out.close();

		} catch (Exception e) {
		}

	}
}
