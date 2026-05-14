import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Práctica 9 — Datos de prueba.
 *
 * 20 clientes sinteticos pero realistas. Reutilizo la misma estructura
 * que en P8 para que se sienta el sistema coherente: los mismos
 * clientes que la P8 gestiona en memoria, aqui se persisten a disco.
 */
public class DatosPrueba {

    public static List<Cliente> generar() {
        List<Cliente> datos = new ArrayList<>(List.of(
            new Cliente(1001, "Ana Gabriela Perez Soto",       "ana.perez@correo.mx",
                    LocalDate.of(2024, 11, 5),  62.5, Cliente.TipoMembresia.PREMIUM),
            new Cliente(1002, "Bruno Hernandez Lara",          "bruno.h@correo.mx",
                    LocalDate.of(2025, 1, 12),  78.0, Cliente.TipoMembresia.BASICA),
            new Cliente(1003, "Carolina Mendez Gomez",         "carolina.m@correo.mx",
                    LocalDate.of(2023, 3, 18),  68.3, Cliente.TipoMembresia.VIP),
            new Cliente(1004, "David Ortega Ruiz",             "david.ortega@correo.mx",
                    LocalDate.of(2022, 6, 1),   88.2, Cliente.TipoMembresia.PREMIUM),
            new Cliente(1005, "Elena Salinas Diaz",            "elena.s@correo.mx",
                    LocalDate.of(2025, 8, 22),  55.0, Cliente.TipoMembresia.BASICA),
            new Cliente(1006, "Fernando Garza Trevino",        "fernando.g@correo.mx",
                    LocalDate.of(2024, 2, 14),  92.5, Cliente.TipoMembresia.VIP),
            new Cliente(1007, "Gabriela Ramos Cantu",          "gabriela.r@correo.mx",
                    LocalDate.of(2025, 4, 30),  61.0, Cliente.TipoMembresia.PREMIUM),
            new Cliente(1008, "Hector Villarreal Montes",      "hector.v@correo.mx",
                    LocalDate.of(2023, 11, 7),  84.0, Cliente.TipoMembresia.BASICA),
            new Cliente(1009, "Isabel Reyes Quiroga",          "isabel.r@correo.mx",
                    LocalDate.of(2024, 7, 15),  58.5, Cliente.TipoMembresia.PREMIUM),
            new Cliente(1010, "Jorge Aguilar Estrada",         "jorge.a@correo.mx",
                    LocalDate.of(2025, 9, 3),   75.3, Cliente.TipoMembresia.BASICA),
            new Cliente(1011, "Karla Sanchez Vega",            "karla.s@correo.mx",
                    LocalDate.of(2024, 12, 18), 63.8, Cliente.TipoMembresia.VIP),
            new Cliente(1012, "Luis Antonio Castillo Cruz",    "luis.c@correo.mx",
                    LocalDate.of(2025, 2, 8),   80.0, Cliente.TipoMembresia.PREMIUM),
            new Cliente(1013, "Mariana Flores Espinoza",       "mariana.f@correo.mx",
                    LocalDate.of(2023, 5, 22),  57.2, Cliente.TipoMembresia.BASICA),
            new Cliente(1014, "Nicolas Romero Ibarra",         "nicolas.r@correo.mx",
                    LocalDate.of(2024, 10, 11), 89.5, Cliente.TipoMembresia.PREMIUM),
            new Cliente(1015, "Olivia Mendoza Acosta",         "olivia.m@correo.mx",
                    LocalDate.of(2025, 6, 25),  64.0, Cliente.TipoMembresia.VIP),
            new Cliente(1016, "Patricio Rios Camacho",         "patricio.r@correo.mx",
                    LocalDate.of(2023, 1, 9),   95.0, Cliente.TipoMembresia.PREMIUM),
            new Cliente(1017, "Quetzalli Vargas Loera",        "quetzalli.v@correo.mx",
                    LocalDate.of(2024, 6, 17),  52.0, Cliente.TipoMembresia.BASICA),
            new Cliente(1018, "Raul Torres Galvan",            "raul.t@correo.mx",
                    LocalDate.of(2025, 3, 28),  82.5, Cliente.TipoMembresia.VIP),
            new Cliente(1019, "Sofia Navarro Pina",            "sofia.n@correo.mx",
                    LocalDate.of(2024, 9, 4),   59.8, Cliente.TipoMembresia.PREMIUM),
            new Cliente(1020, "Tomas Espino Beltran",          "tomas.e@correo.mx",
                    LocalDate.of(2025, 7, 12),  77.0, Cliente.TipoMembresia.BASICA)
        ));

        // Desactivar algunos para que el reporte muestre estado mixto
        datos.get(7).desactivar();   // Hector
        datos.get(12).desactivar();  // Mariana
        datos.get(15).desactivar();  // Patricio

        return datos;
    }
}
