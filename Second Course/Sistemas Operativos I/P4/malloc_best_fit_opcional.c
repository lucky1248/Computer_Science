#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "struct.h"

/*
* DEFINES I VARIABLES GLOBALS
*/
#define ALIGN8(x) (((((x)-1)>>3)<<3)+8)
#define MAGIC     0x12345678
p_meta_data first_element = NULL;
p_meta_data last_element  = NULL;

/*
* SEARCH_AVAILABLE_SPACE
* Dado un número de bytes, devuelve un p_meta_data ya existente disponible para alocar la memoria, seleccionando el mejor
* Como criterio de cual es el mejor, se tiene en cuenta en cual sobra menos espacio de memoria 
*/
p_meta_data search_available_space(size_t size_bytes){
    p_meta_data current = first_element; //Inicializamos un p_meta_data como el primero de la memoria para iterar
    p_meta_data best_fit = NULL; //Inicializamos un p_meta_data para guardar la mejor opción
    while(current){ //Mientras el p_meta_data current no sea NULL
        if(current->available && current->size_bytes >= size_bytes){ //Si está disponible y tiene la suficiente capacidad
            if(!best_fit){ //Si no tenemos aún best_fit 
                best_fit = current; //Le asignamos el p_meta_data en el que nos encontramos
            }
            else{
                if(current->size_bytes < best_fit->size_bytes){//Si el p_meta_data en el que estamos es de un tamaño mas apropiado
                    best_fit = current; //Le asignamos el p_meta_data en el que nos encontramos
                }
            }
        }
        current = current->next; //Pasamos al siguiente p_meta_data
    }
    return best_fit; //Devolvemos el p_meta_data más óptimo, que es NULL si no se ha encontrado ninguno
}

/*
* REQUEST_SPACE
* Dado un número de bytes, devuelve un nuevo p_meta_data disponible para alocar la memoria 
*/
p_meta_data request_space(size_t size_bytes){
    p_meta_data meta_data = (void *) sbrk(0); //Incializamos un p_meta_data con la posición de memòria actual
    if (sbrk(SIZE_META_DATA + size_bytes) == (void *) -1) //Incrementamos el tamaño de la memoria con sbrk 
        return (NULL); //Si sbrk falla, devolvemos NULL
    meta_data->size_bytes = size_bytes; //Almacenamos todos los datos pertinentes en el p_meta_data 
    meta_data->available = 0;
    meta_data->magic = MAGIC;
    meta_data->next = NULL;
    meta_data->previous = NULL; 
    return meta_data; //Devolvemos el p_meta_data
}

/*
* FREE
* Dado un puntero, se libera el espacio de memoria, y si se puede se junta con los espacios de memoria adyacentes
*/
void free(void *ptr){
    if(ptr == NULL){ //Si es un puntero nulo, acabamos la ejecucion
        return;
    }
    p_meta_data meta_data = (p_meta_data)(ptr - SIZE_META_DATA); //Inicializamos un p_meta_data con la supuesta posición de inicio
    if(meta_data->magic != MAGIC){ //Si el valor MAGIC no es correcto, salimos con un mensaje de error
        fprintf(stderr, "Error: Valor MAGIC no correcto \n");    
        exit(0);
    }
    meta_data->available = 1; //Actualizamos el valor de available del p_meta_data a 1
    fprintf(stderr, "Free %zu bytes\n", meta_data->size_bytes); //Mostramos por pantalla la memoria liberada
    if(meta_data->previous){ //Si el p_meta_data tiene uno anterior y este esta disponible, los juntamos.
        if(meta_data->previous->magic != MAGIC){ //Si el valor MAGIC no es correcto, salimos con un mensaje de error
            fprintf(stderr, "Error: Valor MAGIC no correcto \n");    
            exit(0);
        }
        if(meta_data->previous->available == 1){ //Almacenamos todos los nuevos datos pertinentes en el p_meta_data anterior y posterior
            meta_data->previous->size_bytes += meta_data->size_bytes + SIZE_META_DATA; 
            meta_data->previous->next = meta_data->next;
            if(meta_data->next){
                meta_data->next->previous = meta_data->previous;
            }
            else{
                last_element = meta_data->previous;
            }
        }
    }
    if(meta_data->next){ //Si el p_meta_data tiene uno posterior y este esta disponible, los juntamos.
        if(meta_data->next->magic != MAGIC){ //Si el valor MAGIC no es correcto, salimos con un mensaje de error
            fprintf(stderr, "Error: Valor MAGIC no correcto \n");    
            exit(0);
        }    
        if(meta_data->next->available == 1){ //Almacenamos todos los nuevos datos pertinentes en el p_meta_data anterior y posterior
            meta_data->size_bytes += meta_data->next->size_bytes + SIZE_META_DATA; 
            meta_data->next = meta_data->next->next;
            if(meta_data->next){
                meta_data->next->previous = meta_data;
            }
            else{
                last_element = meta_data;
            }
        }
    }
}

/*
* MALLOC
* Dado un número de bytes, reserva un espacio de memoria con ese tamaño, y devuelvo un puntero al inicio de los datos 
*/
void *malloc(size_t size_bytes) {
    void *p; //Declaramos un puntero vacío y un p_meta_data 
    p_meta_data meta_data;
    if (size_bytes <= 0) { //Si el número de bytes es 0 o negativo, acabamos la ejecución
        return NULL;
    }
    size_bytes = ALIGN8(size_bytes); //Canviamos el valor del tamaño para que sea múltiple de 8
    meta_data = search_available_space(size_bytes); //Inicializamos el p_meta_data llamando al método search_available_space
    if (meta_data){ //Si se ha encontrado un bloque libre
        meta_data->available = 0; //Actualizamos el valor de available del p_meta_data a 0
        size_t remaining_size = meta_data->size_bytes - size_bytes - SIZE_META_DATA;
        if((int)remaining_size > 0){ //Si sobra espacio, se hace la partición
            p = (void *) meta_data;
            p_meta_data meta_data_sobrante = (p_meta_data) (p + size_bytes + SIZE_META_DATA);
            meta_data_sobrante->size_bytes = remaining_size;
            meta_data_sobrante->available = 1;
            meta_data_sobrante->magic = MAGIC;
            meta_data_sobrante->previous = meta_data;
            meta_data_sobrante->next = meta_data->next;      
            if (meta_data->next){
                meta_data->next->previous = meta_data_sobrante;
            }
            else{
                last_element = meta_data_sobrante;
            }
            meta_data->next = meta_data_sobrante;
            meta_data->size_bytes = size_bytes;
        }
    }
    else{ //Si no se ha encontrado un bloque libre, es decir, meta_data == NULL
      meta_data = request_space(size_bytes); //Llamamos al método request_space para inicializar el nuevo p_meta_data 
      if (!meta_data){ //Si no se ha podido, se devuelve NULL
        return (NULL);
      }
      if (last_element){ //Si ya hay un ultimo p_meta_data, se actualiza su next
          last_element->next = meta_data;
      }
      meta_data->previous = last_element; //Actualizamos el valor de previos del p_meta_data al ultimo elemento
      last_element = meta_data; //Actualizamos el ultimo elemento como el nuevo p_meta_data creado
      
      if (first_element == NULL){ //Si es el primer elemento, se marca como tal
        first_element = meta_data;
      }
    }
    p = (void *) meta_data; // Se inicializa p como un puntero al p_meta_data
    fprintf(stderr, "Malloc %zu bytes\n", size_bytes); //Mostramos por pantalla la memoria reservada
    return (p + SIZE_META_DATA); //Se devuelve un puntero al espacio de memoria que se puede usar para guardar datos
}

/*
* CALLOC
* Dado un número de elementos y su tamaño, les reserva un espacio de memoria e inicializa a 0, y devuelvo un puntero al inicio de los datos 
*/
void *calloc(size_t nelem, size_t elsize){
    if (nelem <= 0 || elsize <= 0){ //Si el número de elementos o su tamaño es menor o igual que 0 se devuelve NULL 
        return NULL;
    }
    size_t tsize = nelem * elsize; //El tamaño total es el producto de los parámetros
    void *ptr = malloc(tsize); //Se hace un malloc del tamaño i se guarda la devolución en un puntero
    if (ptr != NULL) { //Si el puntero no es NULL, con memset inicializamos todos los datos a 0
        memset(ptr, 0, tsize);
    }
    fprintf(stderr, "Calloc %zu bytes\n", tsize); //Mostramos por pantalla    
    return ptr; //Devolvemos el puntero al inicio de los datos
}

/*
* REALLOC
* Dado un puntero y número de bytes, se reajusta el tamaño del p_meta_data correspondiente 
*/
void *realloc(void *ptr, size_t size_bytes){
    void *p; //Inicializamos un puntero
    if (ptr == NULL) { //Si el puntero es vacio, hacemos simplemente un malloc
        return malloc(size_bytes);
    }
    if (size_bytes <= 0) { //Si el número de bytes es 0 o negativo, se hace un free del p_meta_data
        free(ptr);
        return NULL;
    }
    size_bytes = ALIGN8(size_bytes); //Canviamos el valor del tamaño para que sea múltiple de 8
    p_meta_data meta_data = (p_meta_data)(ptr - SIZE_META_DATA); //Inicializamos un p_meta_data con la supuesta posición de inicio
    if(meta_data->magic != MAGIC){ //Si el valor MAGIC no es correcto, salimos con un mensaje de error
        fprintf(stderr, "Error: Valor MAGIC no correcto \n");    
        exit(0);
    }
    size_t before_size_bytes = meta_data->size_bytes;    
    if (before_size_bytes < size_bytes) { //Si el número de bytes pedidos es mayor a la capacidad que ya tenemos 
        p = malloc(size_bytes); //Creamos un nuevo p_meta_data con malloc
        if (p != NULL) { //Si el puntero no es NULL, con memcpy copiamos todos los datos antiguos a las nuevas posiciones
            memcpy(p, ptr, before_size_bytes);
            free(ptr); //Hacemos un free de los datos anteriores
        }
        return p; //Devolvemos el puntero al inicio de los datos*
    }
    else{
        size_t remaining_size = meta_data->size_bytes - size_bytes - SIZE_META_DATA;
        if ((int)remaining_size > 0){ //Si sobra espacio, se hace la partición
            p_meta_data meta_data_sobrante = (p_meta_data) (ptr + SIZE_META_DATA + size_bytes);
            meta_data_sobrante->size_bytes = remaining_size;
            meta_data_sobrante->available = 1;
            meta_data_sobrante->magic = MAGIC;
            meta_data_sobrante->previous = meta_data;
            meta_data_sobrante->next = meta_data->next;      
            if (meta_data->next){
                meta_data->next->previous = meta_data_sobrante;
            }
            else{
                last_element = meta_data_sobrante;
            }
            meta_data->next = meta_data_sobrante;
            meta_data->size_bytes = size_bytes;
        }
    }
    fprintf(stderr, "Realloc %zu bytes\n", size_bytes); //Mostramos por pantalla
    return ptr; //Si no hacia falta pues el tamaño ya era mayor, hacemos una partición y devolvemos el mismo puntero dado
}
