#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Sep  8 11:22:03 2022

@author: ignasi
"""
from asyncio import wait
import copy
import math

import chess
import board
import numpy as np
import sys
import queue
from typing import List

RawStateType = List[List[List[int]]]

from itertools import permutations


class Aichess():
    """
    A class to represent the game of chess.

    ...

    Attributes:
    -----------
    chess : Chess
        represents the chess game

    Methods:
    --------
    startGame(pos:stup) -> None
        Promotes a pawn that has reached the other side to another, or the same, piece

    """

    def __init__(self, TA, myinit=True):

        if myinit:
            self.chess = chess.Chess(TA, True)
        else:
            self.chess = chess.Chess([], False)

        self.listNextStates = []
        self.listVisitedStates = []
        self.listVisitedSituations = []
        self.pathToTarget = []
        self.currentStateW = self.chess.boardSim.currentStateW
        self.currentStateB = self.chess.boardSim.currentStateB
        self.depthMax = 8
        self.checkMate = False
        self.dictVisitedStates = {}

    def copyState(self, state):
        
        copyState = []
        for piece in state:
            copyState.append(piece.copy())
        return copyState
        
    def isVisitedSituation(self, color, mystate):
        
        if (len(self.listVisitedSituations) > 0):
            perm_state = list(permutations(mystate))

            isVisited = False
            for j in range(len(perm_state)):

                for k in range(len(self.listVisitedSituations)):
                    if self.isSameState(list(perm_state[j]), self.listVisitedSituations.__getitem__(k)[1]) and color == \
                            self.listVisitedSituations.__getitem__(k)[0]:
                        isVisited = True

            return isVisited
        else:
            return False


    def getCurrentStateW(self):

        return aichess.chess.boardSim.currentStateW
    
    def getCurrentStateB(self):

        return aichess.chess.boardSim.currentStateB

    def getListNextStatesW(self, myState):

        self.chess.boardSim.getListNextStatesW(myState)
        self.listNextStates = self.chess.boardSim.listNextStates.copy()

        return self.listNextStates

    def getListNextStatesB(self, myState):
        self.chess.boardSim.getListNextStatesB(myState)
        self.listNextStates = self.chess.boardSim.listNextStates.copy()

        return self.listNextStates

    def isSameState(self, a, b):

        isSameState1 = True
        # a and b are lists
        for k in range(len(a)):

            if a[k] not in b:
                isSameState1 = False

        isSameState2 = True
        # a and b are lists
        for k in range(len(b)):

            if b[k] not in a:
                isSameState2 = False

        isSameState = isSameState1 and isSameState2
        return isSameState

    def isVisited(self, mystate):

        if (len(self.listVisitedStates) > 0):
            perm_state = list(permutations(mystate))

            isVisited = False
            for j in range(len(perm_state)):

                for k in range(len(self.listVisitedStates)):

                    if self.isSameState(list(perm_state[j]), self.listVisitedStates[k]):
                        isVisited = True

            return isVisited
        else:
            return False

    def isWatchedBk(self, currentState):

        self.newBoardSim(currentState)

        bkPosition = self.getPieceState(currentState, 12)[0:2]
        wkState = self.getPieceState(currentState, 6)
        wrState = self.getPieceState(currentState, 2)

        # Si les negres maten el rei blanc, no és una configuració correcta
        if wkState == None:
            return False
        # Mirem les possibles posicions del rei blanc i mirem si en alguna pot "matar" al rei negre
        for wkPosition in self.getNextPositions(wkState):
            if bkPosition == wkPosition:
                # Tindríem un checkMate
                return True
        if wrState != None:
            # Mirem les possibles posicions de la torre blanca i mirem si en alguna pot "matar" al rei negre
            for wrPosition in self.getNextPositions(wrState):
                if bkPosition == wrPosition:
                    return True
        return False
        


    def isWatchedWk(self, currentState):
        self.newBoardSim(currentState)

        wkPosition = self.getPieceState(currentState, 6)[0:2]
        bkState = self.getPieceState(currentState, 12)
        brState = self.getPieceState(currentState, 8)

        # If whites kill the black king , it is not a correct configuration
        if bkState == None:
            return False
        # We check all possible positions for the black king, and chck if in any of them it may kill the white king
        for bkPosition in self.getNextPositions(bkState):
            if wkPosition == bkPosition:
                # That would be checkMate
                return True
        if brState != None:
            # We check the possible positions of the black tower, and we chck if in any o them it may killt he white king
            for brPosition in self.getNextPositions(brState):
                if wkPosition == brPosition:
                    return True

        return False



    def newBoardSim(self, listStates):
        # We create a  new boardSim
        TA = np.zeros((8, 8))
        for state in listStates:
            TA[state[0]][state[1]] = state[2]

        self.chess.newBoardSim(TA)

    def getPieceState(self, state, piece):
        pieceState = None
        for i in state:
            if i[2] == piece:
                pieceState = i
                break
        return pieceState

    def getCurrentState(self):
        listStates = []
        for i in self.chess.board.currentStateW:
            listStates.append(i)
        for j in self.chess.board.currentStateB:
            listStates.append(j)
        return listStates
    
    def getCurrentSimState(self):
        listStates = []
        for i in self.chess.boardSim.currentStateW:
            listStates.append(i)
        for j in self.chess.boardSim.currentStateB:
            listStates.append(j)
        return listStates

    def getNextPositions(self, state):
        # Given a state, we check the next possible states
        # From these, we return a list with position, i.e., [row, column]
        if state == None:
            return None
        if state[2] > 6:
            nextStates = self.getListNextStatesB([state])
        else:
            nextStates = self.getListNextStatesW([state])
        nextPositions = []
        for i in nextStates:
            nextPositions.append(i[0][0:2])
        return nextPositions

    def getWhiteState(self, currentState):
        whiteState = []
        wkState = self.getPieceState(currentState, 6)
        whiteState.append(wkState)
        wrState = self.getPieceState(currentState, 2)
        if wrState != None:
            whiteState.append(wrState)
        return whiteState

    def getBlackState(self, currentState):
        blackState = []
        bkState = self.getPieceState(currentState, 12)
        blackState.append(bkState)
        brState = self.getPieceState(currentState, 8)
        if brState != None:
            blackState.append(brState)
        return blackState

    def getMovement(self, state, nextState):
        # Given a state and a successor state, return the postiion of the piece that has been moved in both states
        if(len(state) > 1):
            if(nextState[0][2]>nextState[1][2]):
                stateAux = nextState[0]
                nextState[0] = nextState[1]
                nextState[1] = stateAux

        pieceState = None
        pieceNextState = None
        for piece in state:
            if piece not in nextState:
                movedPiece = piece[2]
                pieceNext = self.getPieceState(nextState, movedPiece)
                if pieceNext != None:
                    pieceState = piece
                    pieceNextState = pieceNext
                    break

        return [pieceState, pieceNextState]

    def heuristica(self, currentState, color):
        #In this method, we calculate the heuristics for both the whites and black ones
        #The value calculated here is for the whites, 
        #but finally from verything, as a function of the color parameter, we multiply the result by -1
        value = 0

        bkState = self.getPieceState(currentState, 12)
        wkState = self.getPieceState(currentState, 6)
        wrState = self.getPieceState(currentState, 2)
        brState = self.getPieceState(currentState, 8)

        filaBk = bkState[0]
        columnaBk = bkState[1]
        filaWk = wkState[0]
        columnaWk = wkState[1]

        if wrState != None:
            filaWr = wrState[0]
            columnaWr = wrState[1]
        if brState != None:
            filaBr = brState[0]
            columnaBr = brState[1]

        # We check if they killed the black tower
        if brState == None:
            value += 50
            fila = abs(filaBk - filaWk)
            columna = abs(columnaWk - columnaBk)
            distReis = min(fila, columna) + abs(fila - columna)
            if distReis >= 3 and wrState != None:
                filaR = abs(filaBk - filaWr)
                columnaR = abs(columnaWr - columnaBk)
                value += (min(filaR, columnaR) + abs(filaR - columnaR))/10
            # If we are white white, the closer our king from the oponent, the better
            # we substract 7 to the distance between the two kings, since the max distance they can be at in a board is 7 moves
            value += (7 - distReis)
            # If they black king is against a wall, we prioritize him to be at a corner, precisely to corner him
            if bkState[0] == 0 or bkState[0] == 7 or bkState[1] == 0 or bkState[1] == 7:
                value += (abs(filaBk - 3.5) + abs(columnaBk - 3.5)) * 10
            #If not, we will only prioritize that he approahces the wall, to be able to approach the check mate
            else:
                value += (max(abs(filaBk - 3.5), abs(columnaBk - 3.5))) * 10

        # They killed the black tower. Within this method, we consider the same conditions than in the previous condition
        # Within this method we consider the same conditions than in the previous section, but now with reversed values.
        if wrState == None:
            value += -50
            fila = abs(filaBk - filaWk)
            columna = abs(columnaWk - columnaBk)
            distReis = min(fila, columna) + abs(fila - columna)

            if distReis >= 3 and brState != None:
                filaR = abs(filaWk - filaBr)
                columnaR = abs(columnaBr - columnaWk)
                value -= (min(filaR, columnaR) + abs(filaR - columnaR)) / 10
            # If we are white, the close we have our king from the oponent, the better
            # If we substract 7 to the distance between both kings, as this is the max distance they can be at in a chess board
            value += (-7 + distReis)

            if wkState[0] == 0 or wkState[0] == 7 or wkState[1] == 0 or wkState[1] == 7:
                value -= (abs(filaWk - 3.5) + abs(columnaWk - 3.5)) * 10
            else:
                value -= (max(abs(filaWk - 3.5), abs(columnaWk - 3.5))) * 10

        # We are checking blacks
        if self.isWatchedBk(currentState):
            value += 20

        # We are checking whites
        if self.isWatchedWk(currentState):
            value += -20

        # If black, values are negative, otherwise positive
        if not color:
            value = (-1) * value

        return value

    def is_Checkmate(self,currentState,color): 
        #Per no duplicar el codi, ho farem sense tenir en compte el color, només a la inicialització
        #A = el que li toca jugar, "ataca"   D = el que no li toca jugar, "defensa"
        
        #Inicialitzem totes les variables
        if color:
            dkState = self.getPieceState(currentState, 12)
            akState = self.getPieceState(currentState, 6)
            arState = self.getPieceState(currentState, 2)
            drState = self.getPieceState(currentState, 8)
        else:
            akState = self.getPieceState(currentState, 12)
            dkState = self.getPieceState(currentState, 6)
            drState = self.getPieceState(currentState, 2)
            arState = self.getPieceState(currentState, 8) 

        filaAk = akState[0]
        columnaAk = akState[1]
        filaDk = dkState[0]
        columnaDk = dkState[1]

        if arState != None:
            filaAr = arState[0]
            columnaAr = arState[1]
        else:
            return False #No és possible conseguir checkMate amb només el rei
        
        if drState != None:
            filaDr = drState[0]
            columnaDr = drState[1]

        #Només estarem en situació d'escac i mat si el rei D està a les files o columnes 0 i 7
        #Mirem inicialment les columnes
        if (columnaDk == 0) or (columnaDk == 7):
            #La torre A ha d'estar a la mateixa, però no el costat del rei D
            if (columnaDk == columnaAr) and (abs(filaDk - filaAr) > 1):
                #El rei A ha d'estar a dues columnes de separació i a la mateixa fila o una més, si es troba a una cantonada
                if (abs(columnaDk - columnaAk) == 2 and (filaDk == filaAk)) or ((abs(filaDk - filaAk) == 1 and (filaDk == 0 or filaDk == 7))):
                    if drState == None:
                        return True
                    if(filaAk != filaDr and columnaAk != columnaDr):
                        if(filaAk == filaDk):
                            if(filaAr < filaDk and (filaDr < filaAr or filaDr > filaDk)) or (filaAr > filaDk and (filaDr > filaAr or filaDr < filaDk)):
                                return True
                        #Mirem aquí les quatre configuracions diferents (rei que es defensa a una cantonada i al que ataca a una fila diferent)
                        if(filaAr < filaDk and (filaDr < filaAr or filaDr == filaDk)) or (filaAr > filaDk and (filaDr > filaAr or filaDr == filaDk)):
                            return True

        #De manera anàloga mirarem les files    
        if (filaDk == 0) or (filaDk == 7):
            if (filaDk == filaAr and abs(columnaDk - columnaAr) > 1) or ((abs(columnaDk - columnaAk) == 1 and (columnaDk == 0 or columnaDk == 7))):
                if (abs(filaDk - filaAk) == 2 and columnaDk == columnaAk):
                    if drState == None:
                        return True
                    if(columnaAk == columnaDk):
                        if(filaAk != filaDr and columnaAk != columnaDr):
                            if(columnaAr < columnaDk and (columnaDr < columnaAr or columnaDr > columnaDk)) or (columnaAr > columnaDk and (columnaDr > columnaAr or columnaDr < columnaDk)):
                                return True
                        if(columnaAr < columnaDk and (columnaDr < columnaAr or columnaDr == columnaDk)) or (columnaAr > columnaDk and (columnaDr > columnaAr or columnaDr == columnaDk)):
                            return True
                                                           
        #Si no estem en cap situació anterior, retornem false
        return False

    def isDraw(self, currentState):
        #Si només tenim dos peces vol dir que queden els dos reis i, per tant, taules
        if(len(currentState) <= 2):
            return True
        return False
    
    def minimaxGame(self, depthWhite,depthBlack):

        currentState = self.getCurrentState()
        currentStateTuple = tuple(tuple(row) for row in currentState)
        color = True #Primer mouen les blanques
        movSenseMenjar = 0 #Si arriba a 50, són taules
        nombre_fitxes = len(currentState)
        ciclic = False
        self.dictVisitedStates = {currentStateTuple : 1} #Si el valor d'alguna clau arriba a 3, són taules
        self.pathToTarget.append(copy.deepcopy(currentState))
        self.minimaxDecision(depthWhite, depthBlack, color, self.getCurrentState())

        #A cada torn anem cridant la funció minimaxDecision i mirem si és escac i mat o taules
        while (not self.is_Checkmate(self.getCurrentState(), color)) and len(self.getCurrentState()) > 2 and (movSenseMenjar <= 50) and not ciclic:
            currentState = self.getCurrentState()
            currentStateTuple = tuple(tuple(row) for row in currentState)
            self.pathToTarget.append(copy.deepcopy(currentState))

            #Analitzem si s'ha menjat una peça
            if(len(currentState) < nombre_fitxes):
                nombre_fitxes = len(currentState)
                movSenseMenjar = 0
            else:
                movSenseMenjar += 1
            
            #Actualitzem el diccionari d'estats visitats
            if currentStateTuple in self.dictVisitedStates.keys():
                self.dictVisitedStates[currentStateTuple] += 1
                if self.dictVisitedStates[currentStateTuple] == 3:
                    ciclic = True
            else:
                self.dictVisitedStates[currentStateTuple] = 1  

            color = not color
            aichess.chess.board.print_board()
            self.minimaxDecision(depthWhite, depthBlack, color, self.getCurrentState())
        
        currentState = self.getCurrentState()
        self.pathToTarget.append(copy.deepcopy(currentState))

        #Mostrem per pantalla el resultat
        if self.is_Checkmate(currentState, color):
            if color:
               print("Guanyen les blanques!")
            else:
                print("Guanyen les negres!")
        else:
            print("Taules!")
           
        #Mostrem per pantalla el taulell final, així com l'hem anat mostrant cada torn
        aichess.chess.board.print_board()
    
    def minimaxDecision(self, depthWhite, depthBlack, color, currentState):
        if(color):
            depth = depthWhite
        else:
            depth = depthBlack

        self.max_value(0,color,currentState,depth)
    
    def max_value(self, currentDepth, color, currentState, depthMax):
        if(currentDepth == depthMax): #Estat terminal
            return self.heuristica(currentState, color)
        
        v=float("-inf")

        #Creem un nou tauler amb l'estat actual
        self.newBoardSim(currentState)
        
        if(color):

            #Mirem tots els següents estats possibles
            for nextState in self.getListNextStatesW(self.getWhiteState(currentState)):
                
                #Mirem el moviment que es produeix i el fem 
                movement = self.getMovement(self.getWhiteState(currentState),nextState)
                aichess.chess.moveSim(movement[0],movement[1],False)

                #Comprovem si l'estat actual és checkmate
                if(self.is_Checkmate(self.getCurrentSimState(), color)):
                    if(currentDepth == 0):
                        nextMove = movement
                        aichess.chess.move(nextMove[0],nextMove[1])
                        self.newBoardSim(self.getCurrentState())
                    return self.heuristica(self.getCurrentSimState(), color)
                    
                #Comprovem que el moviment sigui legal, és a dir, que no es fiqui en check de les negres
                if not(self.isWatchedWk(self.getCurrentSimState())):
                    s = max(v,self.min_value(currentDepth+1,not color,self.getCurrentSimState(),depthMax))
                    #Si estem a l'estat inicial, guardem el moviment amb millor heurística
                    if(currentDepth == 0):
                        if(v < s):
                            nextMove = movement
                    v = s

                #Tornem a crear un nou tauler amb l'estat inicial, és a dir, desfem el moviment
                self.newBoardSim(currentState)
        
        else:
            #Mirem tots els següents estats possibles
            for nextState in self.getListNextStatesB(self.getBlackState(currentState)):
                #Mirem el moviment que es produeix i el fem 
                movement = self.getMovement(self.getBlackState(currentState),nextState)
                aichess.chess.moveSim(movement[0],movement[1],False)

                #Comprovem si l'estat actual és checkmate
                if(self.is_Checkmate(self.getCurrentSimState(), color)):
                    if(currentDepth == 0):
                        nextMove = movement
                        aichess.chess.move(nextMove[0],nextMove[1])
                        self.newBoardSim(self.getCurrentState())
                    return self.heuristica(self.getCurrentSimState(), color)
       
                #Comprovem que el moviment sigui legal, és a dir, que no es fiqui en check de les blanques
                if not(self.isWatchedBk(self.getCurrentSimState())):
                    s = max(v,self.min_value(currentDepth+1,not color,self.getCurrentSimState(),depthMax))
                    #Si estem a l'estat inicial, guardem el moviment amb millor heurística
                    if(currentDepth == 0):
                        if(v < s):
                            nextMove = movement
                    v = s

                #Tornem a crear un nou tauler amb l'estat inicial, és a dir, desfem el moviment
                self.newBoardSim(currentState)
        
        #Una vegada trobat el millor moviment, el fem al tauler real
        if(currentDepth == 0):
            aichess.chess.move(nextMove[0],nextMove[1])
            self.newBoardSim(self.getCurrentState())

        return v


    def min_value(self, currentDepth, color, currentState, depthMax):
        if(currentDepth == depthMax): #Estat terminal
            return self.heuristica(currentState, not color)
        
        v=float("inf")

        #Creem un nou tauler amb l'estat actual
        self.newBoardSim(currentState)
                
        if(color):

            #Mirem tots els següents estats possibles
            for nextState in self.getListNextStatesW(self.getWhiteState(currentState)):
                
                #Mirem el moviment que es produeix i el fem 
                movement = self.getMovement(self.getWhiteState(currentState),nextState)
                aichess.chess.moveSim(movement[0],movement[1],False)

                #Comprovem si l'estat actual és checkmate
                if(self.is_Checkmate(self.getCurrentSimState(), color)):
                    return self.heuristica(self.getCurrentSimState(), not color) #Estat terminal
                    
                #Comprovem que el moviment sigui legal, és a dir, que no es fiqui en check de les negres
                if not(self.isWatchedWk(self.getCurrentSimState())):
                    s = min(v,self.max_value(currentDepth+1,not color,self.getCurrentSimState(),depthMax))
                    #Si estem a l'estat inicial, guardem el moviment amb millor heurística
                    if(currentDepth == 0):
                        if(v > s):
                            nextMove = movement
                    v = s
 
                #Tornem a crear un nou tauler amb l'estat inicial, és a dir, desfem el moviment
                self.newBoardSim(currentState)
    
        else:

            #Mirem tots els següents estats possibles
            for nextState in self.getListNextStatesB(self.getBlackState(currentState)):

                #Mirem el moviment que es produeix i el fem 
                movement = self.getMovement(self.getBlackState(currentState),nextState)
                aichess.chess.moveSim(movement[0],movement[1],False)

                #Comprovem si l'estat actual és checkmate
                if(self.is_Checkmate(self.getCurrentSimState(), color)):
                    return self.heuristica(self.getCurrentSimState(), not color) #Estat terminal
                  
                #Comprovem que el moviment sigui legal, és a dir, que no es fiqui en check de les blanques
                if not(self.isWatchedBk(self.getCurrentSimState())):
                    s = min(v,self.max_value(currentDepth+1,not color,self.getCurrentSimState(),depthMax))
                    #Si estem a l'estat inicial, guardem el moviment amb millor heurística
                    if(currentDepth == 0):
                        if(v > s):
                            nextMove = movement
                    v = s

                #Tornem a crear un nou tauler amb l'estat inicial, és a dir, desfem el moviment
                self.newBoardSim(currentState)

        #Si això passa, vol dir que no es pot fer cap moviment legal i, per tant, el moviment és dolent
        if(v==float('inf')):
            v = float('-inf')

        return v
        

    def minimax_poda_MixGame(self, depthWhite,depthBlack):
        currentState = self.getCurrentState()
        currentStateTuple = tuple(tuple(row) for row in currentState)
        color = True; #Primer mouen les blanques
        movSenseMenjar = 0
        nombre_fitxes = len(currentState)
        ciclic = False
        self.dictVisitedStates = {currentStateTuple : 1}  
        self.pathToTarget.append(copy.deepcopy(currentState))
        self.minimaxDecision(depthWhite, depthBlack, color, self.getCurrentState())
        
        #A cada torn anem cridant la funció minimaxDecision o alphaBetaPoda decision i mirem si és escac i mat o taules
        while (not self.is_Checkmate(self.getCurrentState(), color)) and len(self.getCurrentState()) > 2 and (movSenseMenjar <= 50) and not ciclic:
            currentState = self.getCurrentState()
            currentStateTuple = tuple(tuple(row) for row in currentState)
            self.pathToTarget.append(copy.deepcopy(currentState))

            #Analitzem si s'ha menjat una peça
            if(len(currentState) < nombre_fitxes):
                nombre_fitxes = len(currentState)
                movSenseMenjar = 0
            else:
                movSenseMenjar += 1
            
            #Actualitzem el diccionari d'estats visitats
            if currentStateTuple in self.dictVisitedStates.keys():
                self.dictVisitedStates[currentStateTuple] += 1
                if self.dictVisitedStates[currentStateTuple] == 3:
                    ciclic = True
            else:
                self.dictVisitedStates[currentStateTuple] = 1  

            color = not color
            aichess.chess.board.print_board()
            #Minimax per les blanques, poda alfa-beta per les negres
            if(color):
                self.minimaxDecision(depthWhite, depthBlack, color, self.getCurrentState())  
            else:  
                self.alphaBetaPodaDecision(depthWhite, depthBlack, color, self.getCurrentState())

        #Mostrem per pantalla el resultat
        if self.is_Checkmate(currentState, color):
            if color:
               print("Guanyen les blanques!")
            else:
                print("Guanyen les negres!")
        else:
            print("Taules!")
           
        #Mostrem per pantalla el taulell final, així com l'hem anat mostrant cada torn
        aichess.chess.board.print_board()

    def alphaBetaPodaGame(self, depthWhite,depthBlack):
        
        currentState = self.getCurrentState()
        currentStateTuple = tuple(tuple(row) for row in currentState)
        color = True; #Primer mouen les blanques
        movSenseMenjar = 0
        nombre_fitxes = len(currentState)
        ciclic = False
        self.dictVisitedStates = {currentStateTuple : 1}  
        self.alphaBetaPodaDecision(depthWhite, depthBlack, color, self.getCurrentState())
        
        #A cada torn anem cridant la funció alphaBetaPodaDecision i mirem si és escac i mat o taules
        while (not self.is_Checkmate(self.getCurrentState(), color)) and len(self.getCurrentState()) > 2 and (movSenseMenjar <= 50) and not ciclic:
            currentState = self.getCurrentState()
            currentStateTuple = tuple(tuple(row) for row in currentState)
            self.pathToTarget.append(copy.deepcopy(currentState))

            #Analitzem si s'ha menjat una peça
            if(len(currentState) < nombre_fitxes):
                nombre_fitxes = len(currentState)
                movSenseMenjar = 0
            else:
                movSenseMenjar += 1
            
            #Actualitzem el diccionari d'estats visitats
            if currentStateTuple in self.dictVisitedStates.keys():
                self.dictVisitedStates[currentStateTuple] += 1
                if self.dictVisitedStates[currentStateTuple] == 3:
                    ciclic = True
            else:
                self.dictVisitedStates[currentStateTuple] = 1  

            color = not color
            aichess.chess.board.print_board()
            self.alphaBetaPodaDecision(depthWhite, depthBlack, color, self.getCurrentState())

        #Mostrem per pantalla el resultat
        if self.is_Checkmate(currentState, color):
            if color:
               print("Guanyen les blanques!")
            else:
                print("Guanyen les negres!")
        else:
            print("Taules!")
           
        #Mostrem per pantalla el taulell final, així com l'hem anat mostrant cada torn
        aichess.chess.board.print_board()
    
    def alphaBetaPodaDecision(self, depthWhite, depthBlack, color, currentState):
        if(color):
            depth = depthWhite
        else:
            depth = depthBlack

        self.alphaBetaMax(0,color,currentState,depth,float('-inf'),float('inf'))

    def alphaBetaMax(self, currentDepth, color, currentState, depthMax, alpha, beta):
        if(currentDepth == depthMax): #Estat terminal
            return self.heuristica(currentState, color)
        
        v=float("-inf")

        #Creem un nou tauler amb l'estat actual
        self.newBoardSim(currentState)
        
        if(color):

            #Mirem tots els següents estats possibles
            for nextState in self.getListNextStatesW(self.getWhiteState(currentState)):
                
                #Mirem el moviment que es produeix i el fem 
                movement = self.getMovement(self.getWhiteState(currentState),nextState)
                aichess.chess.moveSim(movement[0],movement[1],False)

                #Comprovem si l'estat actual és checkmate
                if(self.is_Checkmate(self.getCurrentSimState(), color)):
                    if(currentDepth == 0):
                        nextMove = movement
                        aichess.chess.move(nextMove[0],nextMove[1])
                        self.newBoardSim(self.getCurrentState())
                    return self.heuristica(self.getCurrentSimState(), color)
                    
                #Comprovem que el moviment sigui legal, és a dir, que no es fiqui en check de les negres
                if not(self.isWatchedWk(self.getCurrentSimState())):
                    s = max(v,self.alphaBetaMin(currentDepth+1,not color,self.getCurrentSimState(),depthMax,alpha,beta))
                    #Si estem a l'estat inicial, guardem el moviment amb millor heurística
                    if(currentDepth == 0):
                        if(v < s):
                            nextMove = movement
                    v = s
                    if v >= beta:
                        return v
                    alpha = max(alpha, v)

                #Tornem a crear un nou tauler amb l'estat inicial, és a dir, desfem el moviment
                self.newBoardSim(currentState)
        
        else:
            #Mirem tots els següents estats possibles
            for nextState in self.getListNextStatesB(self.getBlackState(currentState)):
                #Mirem el moviment que es produeix i el fem 
                movement = self.getMovement(self.getBlackState(currentState),nextState)
                aichess.chess.moveSim(movement[0],movement[1],False)

                #Comprovem si l'estat actual és checkmate
                if(self.is_Checkmate(self.getCurrentSimState(), color)):
                    if(currentDepth == 0):
                        nextMove = movement
                        aichess.chess.move(nextMove[0],nextMove[1])
                        self.newBoardSim(self.getCurrentState())
                    return self.heuristica(self.getCurrentSimState(), color)
       
                #Comprovem que el moviment sigui legal, és a dir, que no es fiqui en check de les blanques
                if not(self.isWatchedBk(self.getCurrentSimState())):
                    s = max(v,self.alphaBetaMin(currentDepth+1,not color,self.getCurrentSimState(),depthMax,alpha,beta))
                    #Si estem a l'estat inicial, guardem el moviment amb millor heurística
                    if(currentDepth == 0):
                        if(v < s):
                            nextMove = movement
                    v = s
                    if v >= beta:
                        return v
                    alpha = max(alpha, v)

                #Tornem a crear un nou tauler amb l'estat inicial, és a dir, desfem el moviment
                self.newBoardSim(currentState)
        
        #Una vegada trobat el millor moviment, el fem al tauler real
        if(currentDepth == 0):
            aichess.chess.move(nextMove[0],nextMove[1])
            self.newBoardSim(self.getCurrentState())

        return v
    
    def alphaBetaMin(self, currentDepth, color, currentState, depthMax, alpha, beta):
        if(currentDepth == depthMax): #Estat terminal
            return self.heuristica(currentState, not color)
        
        v=float("inf")

        #Creem un nou tauler amb l'estat actual
        self.newBoardSim(currentState)
                
        if(color):

            #Mirem tots els següents estats possibles
            for nextState in self.getListNextStatesW(self.getWhiteState(currentState)):
                
                #Mirem el moviment que es produeix i el fem 
                movement = self.getMovement(self.getWhiteState(currentState),nextState)
                aichess.chess.moveSim(movement[0],movement[1],False)

                #Comprovem si l'estat actual és checkmate
                if(self.is_Checkmate(self.getCurrentSimState(), color)):
                    return self.heuristica(self.getCurrentSimState(), not color) #Estat terminal
                    
                #Comprovem que el moviment sigui legal, és a dir, que no es fiqui en check de les negres
                if not(self.isWatchedWk(self.getCurrentSimState())):
                    s = min(v,self.alphaBetaMax(currentDepth+1,not color,self.getCurrentSimState(),depthMax,alpha,beta))
                    #Si estem a l'estat inicial, guardem el moviment amb millor heurística
                    if(currentDepth == 0):
                        if(v > s):
                            nextMove = movement
                    v = s
                    if v <= alpha:
                        return v
                    beta = min(beta, v)
 
                #Tornem a crear un nou tauler amb l'estat inicial, és a dir, desfem el moviment
                self.newBoardSim(currentState)
    
        else:

            #Mirem tots els següents estats possibles
            for nextState in self.getListNextStatesB(self.getBlackState(currentState)):

                #Mirem el moviment que es produeix i el fem 
                movement = self.getMovement(self.getBlackState(currentState),nextState)
                aichess.chess.moveSim(movement[0],movement[1],False)

                #Comprovem si l'estat actual és checkmate
                if(self.is_Checkmate(self.getCurrentSimState(), color)):
                    return self.heuristica(self.getCurrentSimState(), not color) #Estat terminal
                  
                #Comprovem que el moviment sigui legal, és a dir, que no es fiqui en check de les blanques
                if not(self.isWatchedBk(self.getCurrentSimState())):
                    s = min(v,self.alphaBetaMax(currentDepth+1,not color,self.getCurrentSimState(),depthMax,alpha,beta))
                    #Si estem a l'estat inicial, guardem el moviment amb millor heurística
                    if(currentDepth == 0):
                        if(v > s):
                            nextMove = movement
                    v = s
                    if v <= alpha:
                        return v
                    beta = min(beta, v)

                #Tornem a crear un nou tauler amb l'estat inicial, és a dir, desfem el moviment
                self.newBoardSim(currentState)

        if(currentDepth == 0):
            aichess.chess.move(nextMove[0],nextMove[1])
            self.newBoardSim(self.getCurrentState())
        
        #Si això passa, vol dir que no es pot fer cap moviment legal i, per tant, el moviment és dolent
        if(v==float('inf')):
            v = float('-inf')

        return v
    
    def expectimaxGame(self, depthWhite, depthBlack):
        currentState = self.getCurrentState()
        currentStateTuple = tuple(tuple(row) for row in currentState)
        color = True; #Primer mouen les blanques
        movSenseMenjar = 0
        nombre_fitxes = len(currentState)
        ciclic = False
        self.dictVisitedStates = {currentStateTuple : 1}  
        self.pathToTarget.append(copy.deepcopy(currentState))
        self.expectimaxDecision(depthWhite, depthBlack, color, self.getCurrentState())
        
        #A cada torn anem cridant la funció minimaxDecision i mirem si és escac i mat o taules
        while (not self.is_Checkmate(self.getCurrentState(), color)) and len(self.getCurrentState()) > 2 and (movSenseMenjar <= 50) and not ciclic:
            currentState = self.getCurrentState()
            currentStateTuple = tuple(tuple(row) for row in currentState)
            self.pathToTarget.append(copy.deepcopy(currentState))

            #Analitzem si s'ha menjat una peça
            if(len(currentState) < nombre_fitxes):
                nombre_fitxes = len(currentState)
                movSenseMenjar = 0
            else:
                movSenseMenjar += 1
            
            #Actualitzem el diccionari d'estats visitats
            if currentStateTuple in self.dictVisitedStates.keys():
                self.dictVisitedStates[currentStateTuple] += 1
                if self.dictVisitedStates[currentStateTuple] == 3:
                    ciclic = True
            else:
                self.dictVisitedStates[currentStateTuple] = 1  

            color = not color
            aichess.chess.board.print_board()

            if(color):
                self.expectimaxDecision(depthWhite, depthBlack, color, self.getCurrentState())
            else:
                self.alphaBetaPodaDecision(depthWhite, depthBlack, color, self.getCurrentState())

        currentState = self.getCurrentState()
        self.pathToTarget.append(copy.deepcopy(currentState))

        #Mostrem per pantalla el resultat
        if self.is_Checkmate(currentState, color):
            if color:
               print("Guanyen les blanques!")
            else:
                print("Guanyen les negres!")
        else:
            print("Taules!")
           
        #Mostrem per pantalla el taulell final, així com l'hem anat mostrant cada torn
        aichess.chess.board.print_board()
    
    def expectimaxDecision(self, depthWhite, depthBlack, color, currentState):
        if(color):
            depth = depthWhite
        else:
            depth = depthBlack

        self.expectimax(0,color,currentState,depth,"max","None")

    def expectimax(self, currentDepth, color, currentState, depthMax, nodeType, previousNodeType):
        nextMove = []
        values = []
        if(currentDepth == depthMax): #Estat terminal
            if(nodeType == "min" or previousNodeType == "max"):
                return self.heuristica(currentState, not color)
            return self.heuristica(currentState, color)
        
        if(nodeType == "max"):
            v=float('-inf')
        else:
            v=float('inf')

        #Creem un nou tauler amb l'estat actual
        self.newBoardSim(currentState)
        
        if(color):

            #Mirem tots els següents estats possibles
            for nextState in self.getListNextStatesW(self.getWhiteState(currentState)):
                #Si estem a un node chance, calculem el valor de cada estat successor i l'afegim a la llista values
                if(nodeType == "chance"):
                    #Si el node anterior era max, el següent serà min i viceversa
                    if(previousNodeType == "max"):
                        values.append(self.expectimax(currentDepth+1, color, self.getCurrentSimState(), depthMax, nodeType="min", previousNodeType="chance"))
                    elif(previousNodeType == "min"):
                        values.append(self.expectimax(currentDepth+1, color, self.getCurrentSimState(), depthMax, nodeType="max", previousNodeType="chance"))

                #Si estem a un node min o max
                else:
                    #Mirem el moviment que es produeix i el fem 
                    movement = self.getMovement(self.getWhiteState(currentState),nextState)
                    aichess.chess.moveSim(movement[0],movement[1],False)

                    #Comprovem si l'estat actual és checkmate
                    if(self.is_Checkmate(self.getCurrentSimState(), color)):
                        if(currentDepth == 0):
                            nextMove = movement
                            aichess.chess.move(nextMove[0],nextMove[1])
                            self.newBoardSim(self.getCurrentState())
                        return self.heuristica(self.getCurrentSimState(), color)

                    #Comprovem que el moviment sigui legal, és a dir, que no es fiqui en check de les negres
                    if not(self.isWatchedWk(self.getCurrentSimState())):
                        #Calculem el maxim o minim dels nodes chance successors
                        if(nodeType == "max"):
                            s = max(v,self.expectimax(currentDepth+1, not color, self.getCurrentSimState(), depthMax, nodeType="chance", previousNodeType="max"))
                        elif(nodeType == "min"):
                            s = min(v, self.expectimax(currentDepth+1, not color, self.getCurrentSimState(), depthMax, nodeType="chance", previousNodeType="min"))
                            
                        #Si estem a l'estat inicial, guardem el moviment amb millor esperança
                        if(currentDepth == 0):
                            if(v < s):
                                nextMove = movement
                        v = s

                #Tornem a crear un nou tauler amb l'estat inicial, és a dir, desfem el moviment
                self.newBoardSim(currentState)

        else:
            #Mirem tots els següents estats possibles
            for nextState in self.getListNextStatesB(self.getBlackState(currentState)):
                #Si estem a un node chance, calculem el valor de cada estat successor i l'afegim a la llista values
                if(nodeType == "chance"):
                    #Si el node anterior era max, el següent serà min i viceversa
                    if(previousNodeType == "max"):
                        values.append(self.expectimax(currentDepth+1, color, self.getCurrentSimState(), depthMax, nodeType="min", previousNodeType="chance"))
                    elif(previousNodeType == "min"):
                        values.append(self.expectimax(currentDepth+1, color, self.getCurrentSimState(), depthMax, nodeType="max", previousNodeType="chance"))
                #Si estem a un node min o max
                else:
                    #Mirem el moviment que es produeix i el fem 
                    movement = self.getMovement(self.getBlackState(currentState),nextState)
                    aichess.chess.moveSim(movement[0],movement[1],False)

                    #Comprovem si l'estat actual és checkmate
                    
                    if(self.is_Checkmate(self.getCurrentSimState(), color)):
                        if(currentDepth == 0):
                            nextMove = movement
                            aichess.chess.move(nextMove[0],nextMove[1])
                            self.newBoardSim(self.getCurrentState())
                        return self.heuristica(self.getCurrentSimState(), color)
                    
                    #Comprovem que el moviment sigui legal, és a dir, que no es fiqui en check de les blanques
                    if not(self.isWatchedBk(self.getCurrentSimState())):
                        #Calculem el maxim o minim dels nodes chance successors
                        if(nodeType == "max"):
                            s = max(v,self.expectimax(currentDepth+1, not color, self.getCurrentSimState(), depthMax, nodeType="chance", previousNodeType="max"))
                        elif(nodeType == "min"):
                            s = min(v, self.expectimax(currentDepth+1, not color, self.getCurrentSimState(), depthMax, nodeType="chance", previousNodeType="min"))

                        #Si estem a l'estat inicial, guardem el moviment amb millor esperança
                        if(currentDepth == 0):
                            if(v < s):
                                nextMove = movement
                        v = s

                    #Tornem a crear un nou tauler amb l'estat inicial, és a dir, desfem el moviment
                self.newBoardSim(currentState)

        #Una vegada coneguts tots els valors dels estats successors calculem l'esperança del node chance
        if(nodeType == "chance"):
            v = self.calculateValue(values)

        if(currentDepth == 0):
            aichess.chess.move(nextMove[0],nextMove[1])
            self.newBoardSim(self.getCurrentState())

        return v

    def mitjana(self, values):
        sum = 0
        N = len(values)
        for i in range(N):
            sum += values[i]

        return sum / N

    def desviacio(self, values, mitjana):
        sum = 0
        N = len(values)

        for i in range(N):
            sum += pow(values[i] - mitjana, 2)

        return pow(sum / N, 1 / 2)

    def calculateValue(self, values):
        
        if len(values) == 0:
            return 0
        mitjana = self.mitjana(values)
        desviacio = self.desviacio(values, mitjana)
        # If deviation is 0, we cannot standardize values, since they are all equal, thus probability willbe equiprobable
        if desviacio == 0:
            # We return another value
            return values[0]

        esperanca = 0
        sum = 0
        N = len(values)
        for i in range(N):
            #Normalize value, with mean and deviation - zcore
            normalizedValues = (values[i] - mitjana) / desviacio
            # make the values positive with function e^(-x), in which x is the standardized value
            positiveValue = pow(1 / math.e, normalizedValues)
            # Here we calculate the expected value, which in the end will be expected value/sum            
            # Our positiveValue/sum represent the probabilities for each value
            # The larger this value, the more likely
            esperanca += positiveValue * values[i]
            sum += positiveValue

        return esperanca / sum
         

if __name__ == "__main__":
    #   if len(sys.argv) < 2:
    #       sys.exit(usage())
    # intialize board
    TA = np.zeros((8, 8))

    TA[7][0] = 2
    TA[7][4] = 6
    TA[0][7] = 8
    TA[0][4] = 12

    # initialise board
    print("stating AI chess... ")
    aichess = Aichess(TA, True)

    print("printing board")
    aichess.chess.boardSim.print_board()

    #Run exercise 1
    #aichess.minimaxGame(3,2)

    #print(aichess.is_Checkmate(aichess.getCurrentState(),True))
    #aichess.alphaBetaPoda(3,3)

    #aichess.minimax_poda_MixGame(4,4)
    aichess.expectimaxGame(3,3)

    aichess.chess.boardSim.print_board()
   
  # Add code to save results and continue with other exercises