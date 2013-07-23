extern "C"
__global__ void playoutMultiLeaf(int *rands, int *numRands, int *board,
		int *boardWidth, int *colorToPlay, int *move, float *wins) {
	//copy board to local

	int l_width = *boardWidth;

	//NOTE!!!!! hardcoded!!! change if you can.....
	int tempBoard[9 * 9];
	for (int j = 0; j < l_width * l_width; j++) {
		tempBoard[j] = board[j];
	}

	int boardFull = 1;
	int wincolor = -1;
	int colorTP = *colorToPlay;

	tempBoard[move[blockIdx.x]] = colorTP;

	if (colorTP == 1){
		colorTP = 2;
	} else {
		colorTP = 1;
	}

	int count = 0;

	while (true) {
		count += 1;
		wincolor = -3;
		//actual playouts
		int n = rands[(blockIdx.x * (*numRands / blockDim.x) + count + threadIdx.x) % *numRands];
		if (tempBoard[n] == 0) {
			tempBoard[n] = colorTP;

			//check end of game.....

			int x = n % l_width;
			int y = n / l_width;
			int counter = 0;
			int xNew;
			int yNew;

			//DownR to UpL

			for (int j = -4; j < 4; j++) {
				xNew = x - j;
				yNew = y - j;
				if ((xNew >= 0) && (xNew < l_width) && (yNew >= 0)
						&& (yNew < l_width)) {
					if (tempBoard[xNew + yNew * l_width] == colorTP) {
						counter += 1;
					} else {
						counter = 0;
					}
					if (counter == 5) {
						break;
					}
					if ((j > 0) && (counter == 0)) {
						break;
					}
				}
			}
			if (counter == 5) {
				wincolor = colorTP;
				break;
			}

			counter = 0;

			//UpR to DownL

			for (int j = -4; j < 4; j++) {
				xNew = x - j;
				yNew = y + j;
				if ((xNew >= 0) && (xNew < l_width) && (yNew >= 0)
						&& (yNew < l_width)) {
					if (tempBoard[xNew + yNew * l_width] == colorTP) {
						counter += 1;
					} else {
						counter = 0;
					}
					if (counter == 5) {
						break;
					}
					if ((j > 0) && (counter == 0)) {
						break;
					}
				}
			}
			if (counter == 5) {
				wincolor = colorTP;
				break;
			}

			counter = 0;

			//horizontal check

			for (xNew = x - 4; xNew < x + 4; xNew++) {
				if ((xNew >= 0) && (xNew < l_width)) {
					if (tempBoard[xNew + y * l_width] == colorTP) {
						counter += 1;
					} else {
						counter = 0;
					}
					if (counter == 5) {
						break;
					}
					if ((xNew > x) && (counter == 0)) {
						break;
					}
				}
			}
			if (counter == 5) {
				wincolor = colorTP;
				break;
			}

			counter = 0;

			//vertical check

			for (yNew = y - 4; yNew < y + 4; yNew++) {
				if ((yNew >= 0) && (yNew < l_width)) {
					if (tempBoard[x + yNew * l_width] == colorTP) {
						counter += 1;
					} else {
						counter = 0;
					}
					if (counter == 5) {
						break;
					}
					if ((yNew > y) && (counter == 0)) {
						break;
					}
				}
			}
			if (counter == 5) {
				wincolor = colorTP;
				break;
			}

			//check if board is full
			for (int i = 0; i < (l_width * l_width); i++) {
				if (tempBoard[i] == 0) {
					boardFull = 0;
					break;
				}
			}
			if (boardFull) {
				wincolor = 0;
				break;
			}
			boardFull = 1;
			if (colorTP == 1) {
				colorTP = 2;
			} else {
				colorTP = 1;
			}
		}
	}
	if (wincolor == *colorToPlay) {
		atomicAdd(&wins[blockIdx.x], (float) 1.0);
	} else if (wincolor == 0) {
		atomicAdd(&wins[blockIdx.x], (float) 0.5);
	}
}
