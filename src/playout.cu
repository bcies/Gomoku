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
//	int count = 0;
//
//	//actual playouts
//	while (count < 1000){
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
//		int color;
//		int win = 0;
//
//		int i;
//		for (int x = 0; x < *boardWidth; x++){
//			for (int y = 0; y < *boardWidth; y++){
//				color = tempBoard[x + y * (*boardWidth)];
//				if (color != 0){
//					win = 1;
//					if ((x >= 4) && (y<= *boardWidth -5)){
//						for (i = 1; i < 5; i++){
//							if (tempBoard[(x-i)+(y+i) * (*boardWidth)] != color){
//								win = 0;
//
//								break;
//							}
//						}
//						if (win){
//							wincolor = color;
//							break;
//						}
//					}
//					win = 1;
//					if (x <= *boardWidth -5 && y <= *boardWidth-5){
//						for (i = 1; i < 5; i++){
//							if (tempBoard[(x+i) + (y+i) * (*boardWidth)] != color){
//								win = 0;
//								break;
//							}
//						}
//						if (win){
//							wincolor = color;
//							break;
//						}
//					}
//					win = 1;
//					if (y <= *boardWidth-5){
//						for (i = 1; i < 5; i++){
//							if (tempBoard[x + (y+i) * (*boardWidth)] != color){
//								win = 0;
//								break;
//							}
//						}
//						if (win){
//							wincolor = color;
//							break;
//						}
//					}
//					win = 1;
//					if (x <= *boardWidth-5){
//						for (i = 1; i < 5; i++){
//							if (tempBoard[x+i+y * (*boardWidth)] != color){
//								win = false;
//								break;
//							}
//						}
//						if (win){
//							wincolor = color;
//							break;
//						}
//					}
//				} else {
//					boardFull = 0;
//				}
//			}
//			if (wincolor != -1){
//				break;
//			}
//		}
//		if (wincolor != -1){
//			break;
//		}
//		count++;
//	}
//	if (boardFull) {
//		atomicAdd(wins, (float)0.5);
//	}
//	else{
//		if (wincolor != -1 && wincolor == colorTP){
//			atomicAdd(wins, (float)1.0);
//		}
//	}
}
