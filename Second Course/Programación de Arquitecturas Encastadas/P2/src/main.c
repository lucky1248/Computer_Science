/******************************
 *
 * Practica_02_PAE Programació de Ports
 * i pràctica de les instruccions de control de flux:
 * "do ... while", "switch ... case", "if" i "for"
 * UB, 02/2021.
 *****************************/

//Incloem les llibreries necessàries pel funcionament del codi
#include <msp432p401r.h>
#include <stdio.h>
#include <stdint.h>
#include <stdbool.h>

//Definim etiquetes que guarden els bits del Port2 corresponents als leds, els bits del Port1 corresponents als dos polsadors, també les IHR i el retard empleat al codi
#define LED_V_BIT BIT0
#define LEDS_RGB (BIT0 + BIT1 + BIT2)
#define SW1_POS 1
#define SW2_POS 4
#define SW1_INT 0x04
#define SW2_INT 0x0A
#define SW1_BIT BIT(SW1_POS)
#define SW2_BIT BIT(SW2_POS)
#define RETRASO 300000

//Definim una variable global que guardarà l’estat en el que es troba el microcontrolador i li assignem l’estat inicial 0, que no fa res.
volatile uint8_t estado = 0;

/**************************************************************************
 * INICIALIZACIÓN DEL CONTROLADOR DE INTERRUPCIONES (NVIC).
 *
 * Sin datos de entrada
 *
 * Sin datos de salida
 *
 **************************************************************************/
void init_interrupciones()
{
    // Configuracion al estilo MSP430 "clasico":
    // --> Enable Port 4 interrupt on the NVIC.
    // Segun el Datasheet (Tabla "6-39. NVIC Interrupts", apartado "6.7.2 Device-Level User Interrupts"),
    // la interrupcion del puerto 1 es la User ISR numero 35.
    // Segun el Technical Reference Manual, apartado "2.4.3 NVIC Registers",
    // hay 2 registros de habilitacion ISER0 y ISER1, cada uno para 32 interrupciones (0..31, y 32..63, resp.),
    // accesibles mediante la estructura NVIC->ISER[x], con x = 0 o x = 1.
    // Asimismo, hay 2 registros para deshabilitarlas: ICERx, y dos registros para limpiarlas: ICPRx.

    //Int. port 1 = 35 corresponde al bit 3 del segundo registro ISER1:
    NVIC->ICPR[1] |= BIT3; //Primero, me aseguro de que no quede ninguna interrupcion residual pendiente para este puerto,
    NVIC->ISER[1] |= BIT3; //y habilito las interrupciones del puerto

}

/**************************************************************************
 * INICIALIZACIÓN DE LOS BOTONES & LEDS DEL BOOSTERPACK MK II.
 *
 * Sin datos de entrada
 *
 * Sin datos de salida
 *
 **************************************************************************/
void init_botons(void)
{
    //Configuramos botones i LED vermell
    //***************************
    P1SEL0 &= ~(BIT0 + BIT1 + BIT4 );    //Els polsadors i el led son GPIOs
    P1SEL1 &= ~(BIT0 + BIT1 + BIT4 );    //Els polsadors i el led son GPIOs

    //LED vermell = P1.0
    P1DIR |= LED_V_BIT;      //El LED es una sortida
    P1OUT &= ~LED_V_BIT;     //El estat inicial del LED es apagat

    //Botó S1 = P1.1 i S2 = P1.4
    P1DIR &= ~(SW1_BIT + SW2_BIT );    //Un polsador es una entrada
    P1REN |= (SW1_BIT + SW2_BIT );     //Pull-up/pull-down pel pulsador
    P1OUT |= (SW1_BIT + SW2_BIT ); //Donat que l'altra costat es GND, volem una pull-up
    P1IE |= (SW1_BIT + SW2_BIT );      //Interrupcions activades
    P1IES &= ~(SW1_BIT + SW2_BIT );    // amb transicio L->H
    P1IFG = 0;                  // Netegem les interrupcions anteriors
    config_RGB_LEDS(); //Cridem al mètode per inicialitzar els LEDs RGB
}

/**************************************************************************
 * DELAY - A CONFIGURAR POR EL ALUMNO - con bucle while
 *
 * Datos de entrada: Tiempo de retraso. 1 segundo equivale a un retraso de 1000000 (aprox)
 *
 * Sin datos de salida
 *
 **************************************************************************/
void delay_t(uint32_t temps)
{
    volatile uint32_t i;

    //El delay es basa en un bucle buit que es repeteix tants cops com es vulgui, ja que per molt ràpid que s’executi, triga un cert temps en fer cada iteració
    for(i = 0; i< temps; i++){
    }
}

/*****************************************************************************
 * CONFIGURACIÓN DE LOS LEDs DEL PUERTO 2. A REALIZAR POR EL ALUMNO
 *
 * Sin datos de entrada
 *
 * Sin datos de salida
 *
 ****************************************************************************/
void config_RGB_LEDS(void)
{

    P2SEL0 &= ~LEDS_RGB;    //Els LEDs RGB són GPIOs
    P2SEL1 &= ~LEDS_RGB;    //Els LEDs RGB són GPIOs

    P2DIR |= LEDS_RGB;  //Són GPIOs de sortida
    P2OUT &= ~LEDS_RGB; //Els LEDS tenen estat inicial apagat

}

void main(void)
{
    //Guardem la variable estat anterior per poder saber quan canviem d’estat, operació i operacio2 serveixen
    uint8_t estado_anterior = 0, operacio, operacio2;

    WDTCTL = WDTPW + WDTHOLD;       // Stop watchdog timer

    //Inicializaciones:
    init_botons();         //Configuramos botones y leds

    init_interrupciones(); //Configurar y activar las interrupciones de los botones

    __enable_interrupts();


    //Bucle principal (infinito):
    while (true)
    {

        if (estado_anterior != estado)  // Dependiendo del valor del estado se encenderá un LED u otro.
        {
            estado_anterior = estado;   //Actualitzem estado_anterior pel pròxim cop que entrem al bucle
        //La variable estado té el cas en el qual estem actualment
            switch (estado){
        //Encenem els LEDs RGB
                case 1:
                    P2OUT |= LEDS_RGB;
                    break;
        //Invertim els LEDs RGB
                case 2:
                    P2OUT ^= LEDS_RGB;
                    break;
                case 3:
            //Si els tres LEDs RGB estan apagats, encenem el LED R (2.0)
                    if ((P2OUT & LEDS_RGB) == 0x00){
                        P2OUT |= BIT0;
                    }
            //Considerem que P2OUT = <7><6><5><4><3><B><G><R>
                    else{
            //Ens guardem a operacio P2OUT desplaçat a l’esquerra un bit
                        operacio = P2OUT << 1;  //operacio = <6><5><4><B><G><R>0
            //Li apliquem la màscara per quedar-nos amb 0000<G><R>0
                        operacio &= LEDS_RGB;
            //Ara apliquem una segona màscara per quedar-nos amb <7><6><5><4><3>000
                        operacio2 = P2OUT & ~LEDS_RGB;
            //Aconseguim que P2OUT sigui <7><6><5><4><3><G><R>0
                        P2OUT = operacio + operacio2;
                    }
            //Assignem l’estat neutre 0 a estado i estado_anterior perquè no entri al if inicial i si tornem a prémer el polsador 2, que torni a entrar
                    estado_anterior = 0;
                    estado = 0;
                default:
                    break;

            }
        }

        P1OUT ^= LED_V_BIT;     // Conmutamos el estado del LED R
        delay_t(RETRASO);       // periodo del parpadeo
    }

}

/**************************************************************************
 * RUTINAS DE GESTION DE LOS BOTONES:
 * Mediante estas rutinas, se detectará qué botón se ha pulsado
 *
 * Sin Datos de entrada
 *
 * Sin datos de salida
 *
 * Actualizar el valor de la variable global estado
 *
 **************************************************************************/

//ISR para las interrupciones del puerto 1:
void PORT1_IRQHandler(void)
{
    static bool impar = false;
    uint8_t flag = P1IV; //guardamos el vector de interrupciones. De paso, al acceder a este vector, se limpia automaticamente.
    P1IE &= ~(SW1_BIT + SW2_BIT ); //interrupciones del boton S1 y S2 en port 1 desactivadas

    switch (flag)
    {
    //Si la interrupció ha estat causada pel polsador 1
    case SW1_INT:
        if (!impar){
            //Si havíem premut un nombre parell de cops el polsador 1, passarà a senar i, per tant, estem en el cas 1
            estado = 1;
            impar = true;
        }
        else{
        //Si havíem premut un nombre senar de cops el polsador 1, passarà a parell i, per tant, estem en el cas 2
            estado = 2;
            impar = false;
        }
        break;
    //Si la interrupció ha estat causada pel polsador 2, estem en el cas 3
    case SW2_INT:
        estado = 3;
        break;
    default:
        break;
    }

    P1IE |= (SW1_BIT + SW2_BIT );   //interrupciones S1 y S2 en port 1 reactivadas
}
