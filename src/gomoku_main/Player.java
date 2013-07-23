package gomoku_main;

import java.util.ArrayList;

public class Player {

	private double timePerMove;
	private boolean multiLeaf;
	private boolean useHeuristics;
	private boolean UCB;
	private long turnPlayouts;
	private int UCT;
	private int blocks;
	private int threads;

	public Player(double timePerMove, boolean useHeuristics, boolean UCB,
			double UCT) {
		this.timePerMove = timePerMove;
		this.useHeuristics = useHeuristics;
		this.UCB = UCB;
		turnPlayouts = 0;
		blocks = 0;
		threads = 0;
	}

	public int getBestMove(Board board, boolean showTree) {
		turnPlayouts = 0;
		SearchTree tree;
		if (blocks == 0) {
			tree = new SearchTree();
			tree.createRootNodes(board, useHeuristics, UCT);
			long currentTime = System.nanoTime();
			long finishTime = (long) (timePerMove * 1000000000) + currentTime;
			while (currentTime < finishTime) {
				if (UCB) {
					tree.expandUCBTunedTree(board);
				} else {
					tree.expandUCTTree(board);
				}
				turnPlayouts++;
				currentTime = System.nanoTime();
			}
		} else {
			CudaTree cudaTree = new CudaTree();
			cudaTree.createRootNodes(board, useHeuristics);
			long currentTime = System.nanoTime();
			long finishTime = (long) (timePerMove * 1000000000) + currentTime;
			while (currentTime < finishTime) {
				if (multiLeaf) {
					int playouts = cudaTree.expandTreeMultiLeaf(board, blocks, threads);
					turnPlayouts += playouts;
				}
				else{
					cudaTree.expandTree(board, blocks, threads);
					turnPlayouts += (blocks * threads);
				}
				currentTime = System.nanoTime();
			}
			tree = cudaTree;
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

	public long getPlayouts() {
		return turnPlayouts;
	}

	public void setCuda(int blocks, int threads, boolean multileaf) {
		this.blocks = blocks;
		this.threads = threads;
		this.multiLeaf = multileaf;
		CudaNode.prepareGPU();
	}

	public boolean setTimePerMove(double time) {
		if (time > 0) {
			timePerMove = time;
			return true;
		}
		return false;
	}
}
