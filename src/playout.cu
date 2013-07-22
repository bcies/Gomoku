extern "C"
__global__ void playout(int *rands, int *numRands, int *board, int *boardWidth, int *colorToPlay, float *wins)
{

	atomicAdd(wins, (float)1.0);

//	//copy board to local
//
//	//NOTE!!!!! hardcoded!!! change if you can.....
//	int tempBoard[9*9];
//	for (int j = 0; j < 9*9; j++){
//		tempBoard[j] = board[j];
//	}
//
//	int boardFull = 1;
//	int wincolor = -1;
//	int colorTP = *colorToPlay;
//
//
//	//actual playouts
//		int n = rands[threadIdx.x % *numRands];
//		if (tempBoard[n] == 0){
//			tempBoard[n] = colorTP;
//			if (colorTP == 1){
//				colorTP = 2;
//			}
//			else {
//				colorTP = 1;
//			}
//		}
//
//		//check end of game.....
//

//
//			atomicAdd(wins, (float)1.0);
}
