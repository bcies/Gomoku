package gomoku_main;

import java.util.ArrayList;
import java.util.List;

public class CudaTree extends SearchTree {

	public CudaTree() {
		treeNodes = new ArrayList<SearchNode>();
		totalPlayouts = 0;
	}

	public void createRootNodes(Board board, boolean useHeuristics) {
		for (int i = 0; i < board.getBoardArea(); i++) {
			if (board.isLegalMove(i)) {
				treeNodes.add(new CudaNode(i, board.getColorToPlay()));
			}
		}
		if (useHeuristics) {
			applyHeuristic(board);
		}
	}

	public void expandTree(Board board, int blocks, int threads) {
		Board tempBoard = new Board();
		tempBoard.copyBoard(board);
		double bestScore = 0;
		int bestIndex = 0;
		double UCBScore;
		for (int i = 0; i < treeNodes.size(); i++) {
			if (treeNodes.get(i).isExhausted()) {
				UCBScore = -2;
			} else if (treeNodes.get(i).getPlayouts() != 0) {
				if (treeNodes.get(i).isFinalNode()) {
					UCBScore = 0;
				} else {
					UCBScore = UCBSearchValue(totalPlayouts, tempBoard, i);
				}
			} else {
				UCBScore = 0.45 + Math.random() * 0.1;
			}
			if (UCBScore > bestScore) {
				bestScore = UCBScore;
				bestIndex = i;
			}
		}

		if (treeNodes.get(bestIndex).getPlayouts() == 0) {
			CudaNode node = (CudaNode) treeNodes.get(bestIndex);
			node.playout(tempBoard, blocks, threads);
			totalPlayouts += blocks * threads;
		} else {
			CudaNode node = (CudaNode) treeNodes.get(bestIndex);
			node.traverseNode(tempBoard, blocks, threads);
		}
	}

	public int expandTreeMultiLeaf(Board board, int blocks, int threads) {
		Board tempBoard = new Board();
		tempBoard.copyBoard(board);
		double UCBScore;
		int blocksxthreads = blocks * threads;
		List<Double> sortedScore = new ArrayList<Double>();
		List<Integer> sortedIndex = new ArrayList<Integer>();
		for (int i = 0; i < treeNodes.size(); i++) {
			if (treeNodes.get(i).isExhausted()) {
				UCBScore = -2;
			} else if (treeNodes.get(i).getPlayouts() != 0) {
				if (treeNodes.get(i).isFinalNode()) {
					UCBScore = 0;
				} else {
					UCBScore = UCBSearchValue(totalPlayouts, tempBoard, i);
				}
			} else {
				UCBScore = 0.45 + Math.random() * 0.1;
			}
			boolean flag = true;
			for (int j = 0; j < sortedScore.size(); j++) {
				if (UCBScore > sortedScore.get(j)) {
					sortedScore.add(j, UCBScore);
					sortedIndex.add(j, i);
					flag = false;
					break;
				}
			}
			if (flag) {
				sortedScore.add(UCBScore);
				sortedIndex.add(i);
			}
		}

		if (treeNodes.get(sortedIndex.get(0)).getPlayouts() == 0) {

			int[] bestIndex = new int[blocks];
			int[] bestMove = new int[blocks];
			int count = 0;
			int i = 0;
			while (count < blocks) {
				if (i < sortedScore.size()) {
					if (treeNodes.get(sortedIndex.get(i)).getPlayouts() == 0) {
						tempBoard.play(treeNodes.get(sortedIndex.get(i))
								.getMove());
						int winner = tempBoard.getWinner();
						if (winner != Board.VACANT) {
							treeNodes.get(sortedIndex.get(i))
									.setFinalNode(true);
							if (winner == treeNodes.get(sortedIndex.get(i))
									.getColor()) {
								treeNodes.get(sortedIndex.get(i)).setWinRate(
										1.0);
							}
							if (winner == -1) {
								// If the result is a tie.
								treeNodes.get(sortedIndex.get(i)).setWinRate(
										0.65);
							}
							treeNodes.get(sortedIndex.get(i)).setPlayouts(1);
						} else {
							bestIndex[count] = sortedIndex.get(i);
							bestMove[count] = treeNodes.get(sortedIndex.get(i))
									.getMove();
							count++;
						}
						tempBoard.setVacant(treeNodes.get(sortedIndex.get(i))
								.getMove());
					}
				} else {
					bestIndex[count] = -1;
					bestMove[count] = -1;
					count++;
				}
				i++;
			}
			float[] wins = PlayoutMethods.playoutMultiLeaf(board, blocks,
					threads, bestMove);
			int formerPlayouts = totalPlayouts;
			int sumThreads = 0;
			for (i = 0; i < bestIndex.length; i++) {
				if (bestIndex[i] != -1) {
					sumThreads += threads;
					double value = (treeNodes.get(bestIndex[i]).getWinRate()
							* formerPlayouts + wins[i])
							/ (sumThreads+formerPlayouts);
					treeNodes.get(bestIndex[i]).setWinRate(value);
					treeNodes.get(bestIndex[i])
							.setPlayouts(
									treeNodes.get(bestIndex[i]).getPlayouts()
											+ threads);
					sumThreads += threads;
					
				}
			}
			totalPlayouts += sumThreads;
			return sumThreads;
		} else {
			CudaNode node = (CudaNode) treeNodes.get(sortedIndex.get(0));
			double[] pair = node.traverseNodeMultiLeaf(tempBoard, blocks, threads);
			return (int) pair[0];
		}
	}

}
