package abans;

public class CalculadoraEntrada {

    public CalculadoraEntrada() {}

    public String mostrarPerOnEntrar(String temps){
    if (temps == "mati") return mostrarPerOnEntrarMati();
    else if (temps == "migdia") return mostrarPerOnEntrarMigdia();
    else if (temps == "tarda") return mostrarPerOnEntrarTarda();
    else if (temps == "nit") return mostrarPerOnEntrarVespre();
    return mostrarPerOnEntrarNit();
    }

    public String mostrarPerOnEntrarMati(){
        return "Entra amb el tren";
    }

    public String mostrarPerOnEntrarMigdia(){
        return "Entra per la Gran Via";    }

    public String mostrarPerOnEntrarTarda(){
        return "Entra per la Gran Via";    }

    public String mostrarPerOnEntrarVespre(){
        return "Entra per la Ronda";
    }

    public String mostrarPerOnEntrarNit(){
        return "Entra per la Gran Via";
    }

}
