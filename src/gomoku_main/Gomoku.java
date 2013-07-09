package gomoku_main;

import java.util.Scanner;
import java.util.StringTokenizer;

public class Gomoku {
	
	private static Board board;
	
	public static void main(String args[]) {
		board = new Board();
		Player player = new Player();
		String command;
		StringTokenizer token;
		Scanner in = new Scanner(System.in);
		while(!board.hasWinner()) {
			command = in.nextLine();
			if(command.contains("showboard")) {
				System.out.println(board);
				switch(board.getColorToPlay()) {
				case Board.BLACK:
					System.out.print("Black");
					break;
				case Board.WHITE:
					System.out.print("White");
					break;
				}
				System.out.println("'s turn:");
			} else if(command.contains("play ")) {
				token = new StringTokenizer(command, " ");
				token.nextToken();
				if(!board.play(token.nextToken())) {
					System.out.println("Invalid move.\n");
				}
			} else if(command.contains("genmove")) {
				int playerMove = player.getBestMove(board);
				System.out.println("Player played at " + board.indexToString(playerMove));
				board.play(playerMove);
			} else if(command.contains("quit")) {
				System.exit(0);
			}
		}
		System.out.println(board);
		if(board.getWinner() == Board.BLACK) {
			System.out.println("Black Wins!");
		} else {
			System.out.println("White Wins!");
		}
	}
}
