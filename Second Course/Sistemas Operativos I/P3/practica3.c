/**********************************************
 * Víctor Sort y Jose Maria Fernández
 * SO - Práctica 3
 * 04/2023
***********************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <time.h>
#include <sys/types.h>
#include <sys/wait.h>


#define MAX_LEN 100

int escucha_activa = 0;

/* Creación de estructuras para las opciones y las reglas */
typedef struct {
  int id; 
  char nom[MAX_LEN];
} opcio;

typedef struct {
  int id1; 
  int id2; 
  char descripcion[MAX_LEN];
} reglas;

/* Declaración de las funciones */
int leer_opciones(const char *filename, opcio *array, int max_size);
int leer_reglas(const char *filename, reglas *array, int max_size);
void consumidor(int N, opcio *opciones, reglas *reglas, int ret1, int ret2, int fd1[], int fd2[], int num_opciones, int num_reglas);
void productor(int N, int fd[], int num_opciones);
const char* mostrar_opcion(opcio *opciones, int id, int num_opciones);
const int competir(reglas *reglas, int valor1, int valor2, int num_reglas, opcio *opciones, int num_opciones);


/**********************************************
 * SIGUSR1
***********************************************/
void sigusr1(int signo){
  escucha_activa = 1; //Se pone esta variable global a 1 para la implementación de la escucha aciva
}

/**********************************************
 * MAIN
***********************************************/
int main(int argc, char *argv[]){
  /* Declaración de las variables */  
  char *fichero_opciones, *fichero_reglas;
  int N, num_opciones, num_reglas;
  int fd1[2],fd2[2]; //También se podria ha como una matriz 2 x 2: fd[2][2]
  int ret1, ret2;
  opcio o_array[MAX_LEN];
  reglas r_array[MAX_LEN];

  /* Comprobación de número de parámetros */
  if (argc != 4) {
    printf("ERROR: Número incorrecto de argumentos\n");
    printf("Argumentos esperados: 3\n");
    printf("Argumentos proporcionados: %d\n", argc-1);
    printf("%s <fichero_opciones> <fichero_reglas> <N>\n", argv[0]);
    exit(1);
  }

  /* Lectura de parámetros y comprobación de su validez */  
  fichero_opciones = argv[1];
  fichero_reglas = argv[2];
  N = atoi(argv[3]);
  if (N < 1) {
    printf("N no puede ser cero o negativo!\n");
    exit(1);
  }

  /* Lectura del fichero de opciones y comprobación de su validez */
  num_opciones = leer_opciones(fichero_opciones, o_array, MAX_LEN);
  if(num_opciones == -1){
    printf("Error leyendo el fichero de opciones!\n");
    exit(1);
  }


  /* Lectura del fichero de reglas y comprobación de su validez */
  num_reglas = leer_reglas(fichero_reglas, r_array, MAX_LEN);
  if(num_reglas == -1){
    printf("Error leyendo el fichero de reglas!\n");
    exit(1);
  }

  /* Creación de las tuberias para la comunicación entre los procesos */
  pipe(fd1);
  pipe(fd2);

  /* Registro de la función para manejar la señal SIGUSR1 */
  signal(SIGUSR1, sigusr1);

  /* Creación de los 2 procesos hijos y llamada a sus funciones de comportamiento */
  ret1 = fork();
  if(ret1 == 0){
    productor(N,fd1,num_opciones); //El primer hijo será el jugador 1
  }
  else{
    ret2 = fork();
    if(ret2 == 0){
      productor(N,fd2,num_opciones); //El segundo hijo será el jugador 2
    }
    else{
      consumidor(N,o_array,r_array, ret1, ret2, fd1, fd2, num_opciones, num_reglas); //El padre será el árbitro 
      wait(NULL); //Esperamos a que el hijo 1 termine su ejecución
      wait(NULL); //Esperamos a que el hijo 2 termine su ejecución
    }
  }
  
  /* Cierre de las tuberías */
  close(fd1[0]);
  close(fd1[1]);  
  close(fd2[0]);
  close(fd2[1]); 

  /* Finalización de la ejecución del proceso padre */  
  return 0;
}


/**********************************************
 * LEER_OPCIONES
 * Código ya dado que sirve para leer el fichero de las opciones 
***********************************************/
int leer_opciones(const char *filename, opcio *array, int max_size){
    FILE *fp;
    char line[MAX_LEN];
    char *token;
    int count = 0;
    fp = fopen(filename, "r");
    if (fp == NULL) {
        return -1;
    }
    while (fgets(line, MAX_LEN, fp) != NULL && count < max_size) {
      token = strtok(line, ",");
      array[count].id = atoi(token);
      token = strtok(NULL, ",");
      token[strcspn(token, "\r\n")] = 0;  // remove newline character
      strncpy(array[count].nom, token, MAX_LEN - 1);
      array[count].nom[MAX_LEN - 1] = '\0'; // Ensure null-termination
      count++;
    }
    fclose(fp);
    return count;
}


/**********************************************
 * LEER_REGLAS
 * Código ya dado que sirve para leer el fichero de las reglas 
***********************************************/
int leer_reglas(const char *filename, reglas *array, int max_size){
    FILE *fp;
    char line[MAX_LEN];
    char *token;
    int count = 0;
    fp = fopen(filename, "r");
    if (fp == NULL) {
        return -1;
    }
    while (fgets(line, MAX_LEN, fp) != NULL && count < max_size) {
        token = strtok(line, ",");
        array[count].id1 = atoi(token);
        token = strtok(NULL, ",");
        array[count].id2 = atoi(token);
        token = strtok(NULL, ",");
        token[strcspn(token, "\r\n")] = 0;  // remove newline character
        strncpy(array[count].descripcion, token, MAX_LEN - 1);
        array[count].descripcion[MAX_LEN - 1] = '\0'; // Ensure null-termination
        count++;
    }
    fclose(fp);
    return count;
}


/**********************************************
 * CONSUMIDOR
  * Código del padre - árbitro
***********************************************/
void consumidor(int N, opcio *opciones, reglas *reglas, int ret1, int ret2, int fd1[], int fd2[], int num_opciones, int num_reglas){

  /* Declaración de las variables */
  int i,valor1,valor2;
  int victorias1=0, victorias2=0, empate=0, ganador_turno;
  
  /* Generación de las partidas */
  for(i=1; i <= N; i++){
    printf("* TURNO %d\n", i);
    kill(ret1, SIGUSR1); //Se envia una señal para  que juegue el jugador 1
    read(fd1[0], &valor1, sizeof(valor1)); //Se espera a que el jugador uno escriba en la tuberia su opción
    printf("El Jugador 1 ha escogido %s\n", mostrar_opcion(opciones,valor1,num_opciones)); //Se muestra por pantalla llamando a mostrar_opcion
    kill(ret2, SIGUSR1); //Se repite el proceso para el segundo jugador
    read(fd2[0], &valor2, sizeof(valor2));
    printf("El Jugador 2 ha escogido %s\n", mostrar_opcion(opciones,valor2,num_opciones));
    ganador_turno = competir(reglas,valor1,valor2,num_reglas,opciones,num_opciones); //Se enfrentan entre si llamando a competir
    
    /* Switch con los casos posibles, actualizando los contadores */    
    switch(ganador_turno){
      case(0):
        printf("Empate!\n");
        empate++;
        break;
      case(1):
        printf("Gana el Jugador 1!\n");
        victorias1++;
        break;
      case(2):
        printf("Gana el Jugador 2!\n");
        victorias2++;
    }
  }    
  
  /* Muestra de los resultados totales por pantalla */
  printf("EL JUGADOR 1 HA GANADO %d TURNOS\n", victorias1);
  printf("EL JUGADOR 2 HA GANADO %d TURNOS\n", victorias2);
  printf("HA HABIDO %d EMPATES\n", empate);
  if(victorias1 > victorias2){
    printf("GANA EL JUGADOR 1!!!\n");
  }
  else if(victorias1 < victorias2){
    printf("GANA EL JUGADOR 2!!!\n");
  }
  else{
    printf("EMPATE!!!\n");
  }

}



/**********************************************
 * MOSTRAR_OPCION
 * Este código recibe un entero y devuelve el nombre correspondiente de la opcion en opciones.csv
***********************************************/
const char* mostrar_opcion(opcio *opciones, int id, int num_opciones){
  int i;
  for (i = 0; i < num_opciones; i++){
    if (opciones[i].id == id){
      return opciones[i].nom;
    }
  }
  return NULL;
}


/**********************************************
 * COMPETIR
 * Este código recibe dos enteros, los busca si estan en una misma fila en reglas.csv,
 * con importancia al orden, y muestra por pantalla el suceso correspondiente
 * Si no estan, se trata de un empate 
 * Devuelve el número del jugador que ha ganado o un 0 si se produce un empate
***********************************************/
const int competir(reglas *reglas, int valor1, int valor2, int num_reglas, opcio *opciones, int num_opciones){
  int i;
  for (i = 0; i < num_reglas; i++){
    if (reglas[i].id1 == valor1 && reglas[i].id2 == valor2){
      printf("%s %s %s\n",mostrar_opcion(opciones,valor1,num_opciones),reglas[i].descripcion,mostrar_opcion(opciones,valor2,num_opciones));
      return 1;
    }
    if (reglas[i].id1 == valor2 && reglas[i].id2 == valor1){
      printf("%s %s %s\n",mostrar_opcion(opciones,valor2,num_opciones),reglas[i].descripcion,mostrar_opcion(opciones,valor1,num_opciones));
      return 2;
    }
  }
  return 0;
}


/**********************************************
 * PRODUCTOR
 * Código de los hijos - jugadores
***********************************************/
void productor(int N, int fd[], int num_opciones){
  
  /* Declaración de las variables */
  int i, value;

  /* Creación de la semilla random */
  srand(getpid());

  /* Generación de la partida */
  for(i = 0; i < N; i++){
    while (!escucha_activa) {}; //Esperamos a que el proceso reciba una señal y la variable global se ponga a 1
    escucha_activa = 0;
    value = rand() % num_opciones; //Seleccionamos una opción numérica
    write(fd[1], &value, sizeof(value)); //La escribimos en la tubería
  }
}
