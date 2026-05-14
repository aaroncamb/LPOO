import java.time.LocalDate;
import java.util.List;

/**
 * Práctica 11 — Datos de prueba.
 *
 * 12 clientes para mostrar al iniciar la app. Variedad suficiente para
 * que el filtrado y el ordenamiento muestren resultados interesantes.
 */
public class DatosPrueba {

    public static List<Cliente> generar() {
        return List.of(
            new Cliente(1001, "Ana Gabriela Perez Soto",  "ana.perez@correo.mx",
                    LocalDate.of(2024, 11, 5),  62.5, Cliente.TipoMembresia.PREMIUM),
            new Cliente(1002, "Bruno Hernandez Lara",     "bruno.h@correo.mx",
                    LocalDate.of(2025, 1, 12),  78.0, Cliente.TipoMembresia.BASICA),
            new Cliente(1003, "Carolina Mendez Gomez",    "carolina.m@correo.mx",
                    LocalDate.of(2023, 3, 18),  68.3, Cliente.TipoMembresia.VIP),
            new Cliente(1004, "David Ortega Ruiz",        "david.ortega@correo.mx",
                    LocalDate.of(2022, 6, 1),   88.2, Cliente.TipoMembresia.PREMIUM),
            new Cliente(1005, "Elena Salinas Diaz",       "elena.s@correo.mx",
                    LocalDate.of(2025, 8, 22),  55.0, Cliente.TipoMembresia.BASICA),
            new Cliente(1006, "Fernando Garza Trevino",   "fernando.g@correo.mx",
                    LocalDate.of(2024, 2, 14),  92.5, Cliente.TipoMembresia.VIP),
            new Cliente(1007, "Gabriela Ramos Cantu",     "gabriela.r@correo.mx",
                    LocalDate.of(2025, 4, 30),  61.0, Cliente.TipoMembresia.PREMIUM),
            new Cliente(1008, "Hector Villarreal Montes", "hector.v@correo.mx",
                    LocalDate.of(2023, 11, 7),  84.0, Cliente.TipoMembresia.BASICA),
            new Cliente(1009, "Isabel Reyes Quiroga",     "isabel.r@correo.mx",
                    LocalDate.of(2024, 7, 15),  58.5, Cliente.TipoMembresia.PREMIUM),
            new Cliente(1010, "Jorge Aguilar Estrada",    "jorge.a@correo.mx",
                    LocalDate.of(2025, 9, 3),   75.3, Cliente.TipoMembresia.BASICA),
            new Cliente(1011, "Karla Sanchez Vega",       "karla.s@correo.mx",
                    LocalDate.of(2024, 12, 18), 63.8, Cliente.TipoMembresia.VIP),
            new Cliente(1012, "Luis Antonio Castillo Cruz", "luis.c@correo.mx",
                    LocalDate.of(2025, 2, 8),   80.0, Cliente.TipoMembresia.PREMIUM)
        );
    }
}
