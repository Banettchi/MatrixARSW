# **Concurrent Matrix Escape**

Colombian School of Engineering Julio Garavito

Diego Alejandro Montes Bonilla

Software Architectures

---

## **Game Description**

Matrix Escape is a game where you control the setup of a simulation in which Neo tries to escape the Matrix. Unlike a turn-based game, here Neo and the Agents move simultaneously in a race against time using concurrent execution threads.

---

## **Game Rules**

### **Objective**
- Neo (N): Must reach any telephone to escape the Matrix.
- Agents (A): Must capture Neo before he escapes.

### **Board Elements**
- N = Neo (Green square)
- A = Agent (Red square)
- T = Telephone (Blue square)
-.# = Wall (Grey striped block)

### **Board**
- Size: 8x8 grid.
- The player can place the elements manually by clicking on the board or use the Generate Random button.
- Neo and Agents: Move on their own by calculating the shortest path using the BFS algorithm.

---

## **How Concurrency Works**

The game is not turn-based. Neo and each of the Agents are an independent thread that inherits from the abstract class PersonajeMovil.

1. Non-Deterministic Race: All threads compete to move. Each time they take a step, they sleep for a random amount of time. The thread that sleeps the least is the fastest and wins the race to the destination cell.
2. Mutual Exclusion: The Tablero class serves as shared memory. All movements that modify the matrix are synchronized to prevent race conditions where two characters try to occupy the same cell at the same time.

---

## **Architectural Improvements (Single Responsibility Principle)**

1. Isolated BFS: Instead of having all the massive algorithmic logic in the Tablero, the routing algorithm (BFS) was extracted to the threads base class PersonajeMovil.
2. Snapshots: Threads ask the board for a photo and calculate their route on it. This prevents locking the entire board while a thread thinks, which drastically improves performance and concurrent fluidity.
3. Decoupled UI: The graphical interface VentanaJuego uses standard Swing components with dark colors applied directly, reducing the complexity of the graphical code and separating the view from the model.

---

![Game UI](assets/game_ui.png)
