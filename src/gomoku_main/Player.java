package gomoku_main;

import java.util.ArrayList;

public class Player {

	private int playouts;
	private boolean useHeuristics;
	private int UCT;

	public Player(int playouts, boolean useHeuristics, double UCT) {
		this.playouts = playouts;
		this.useHeuristics = useHeuristics;
	}

	public int getBestMove(Board board, boolean showTree) {
		SearchTree tree = new SearchTree();
		tree.createRootNodes(board, useHeuristics, UCT);
		for (int i = 0; i < playouts; i++) {
			tree.expandUCTTree(board);
		}
		ArrayList<SearchNode> nodes = tree.getNodes();
		int bestNodeIndex = 0;
		for (int i = 1; i < nodes.size(); i++) {
			if (nodes.get(i).getWinRate() > nodes.get(bestNodeIndex)
					.getWinRate()) {
				bestNodeIndex = i;
			}
		}
		if (showTree) {
			System.out.println(tree);
		}
		return nodes.get(bestNodeIndex).getMove();
	}
}
