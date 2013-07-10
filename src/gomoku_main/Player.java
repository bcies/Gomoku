package gomoku_main;

import java.util.ArrayList;

public class Player {

	public int getBestMove(Board board) {
		int playouts = 2000;
		SearchTree tree = new SearchTree();
		for (int i = 0; i < playouts; i++) {
			tree.createRootNodes(board);
			tree.expandUCTTree(board);
		}
		ArrayList<SearchNode> nodes = tree.getNodes();
		int bestNodeIndex = 0;
		for (int i = 1; i < nodes.size(); i++) {
			if (nodes.get(i).getWins() > nodes.get(bestNodeIndex).getWins()) {
				bestNodeIndex = i;
			}
		}
		return nodes.get(bestNodeIndex).getMove();
	}
}
