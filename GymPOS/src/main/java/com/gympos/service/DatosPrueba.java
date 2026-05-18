package com.gympos.service;

import com.gympos.model.Cliente;
import com.gympos.model.ClaseGrupal;
import com.gympos.model.Equipo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * GymPOS - Datos de prueba precargados.
 *
 * 20 clientes, 8 clases grupales y 12 equipos para que la app tenga
 * contenido significativo al primer arranque. La consigna pide 20+
 * registros de prueba; entrego 40 distribuidos.
 *
 * Las membresias se crean por el SistemaMembresias al cargar los
 * clientes (uno por cliente, segun su tipoMembresia).
 */
public class DatosPrueba {

    /** Genera los 20 clientes iniciales. */
    public static List<Cliente> generarClientes() {
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

        // Acumulado de puntos previos (simulado para que el sistema de
        // recompensas tenga datos al iniciar)
        datos.get(0).agregarPuntos(450);   // Ana
        datos.get(2).agregarPuntos(820);   // Carolina
        datos.get(5).agregarPuntos(610);   // Fernando
        datos.get(10).agregarPuntos(390);  // Karla
        datos.get(17).agregarPuntos(720);  // Raul

        // Desactivar algunos
        datos.get(7).setActivo(false);   // Hector
        datos.get(12).setActivo(false);  // Mariana

        return datos;
    }

    /** Genera 8 clases grupales programadas para los proximos dias. */
    public static List<ClaseGrupal> generarClasesGrupales() {
        LocalDate base = LocalDate.now();
        return new ArrayList<>(List.of(
            new ClaseGrupal(1, "Yoga matutino",       "Lic. Roberto Avila",
                    base.plusDays(1).atTime(7, 0),  15, 150),
            new ClaseGrupal(2, "Spinning HIIT",       "Lic. Marta Soto",
                    base.plusDays(1).atTime(18, 0), 12, 200),
            new ClaseGrupal(3, "CrossFit",            "Lic. Diego Cantu",
                    base.plusDays(2).atTime(7, 0),  10, 250),
            new ClaseGrupal(4, "Zumba",               "Lic. Andrea Lopez",
                    base.plusDays(2).atTime(19, 0), 20, 100),
            new ClaseGrupal(5, "Pilates",             "Lic. Roberto Avila",
                    base.plusDays(3).atTime(9, 0),  12, 180),
            new ClaseGrupal(6, "Boxeo",               "Lic. Carlos Mendez",
                    base.plusDays(4).atTime(17, 0),  8, 250),
            new ClaseGrupal(7, "Funcional",           "Lic. Diego Cantu",
                    base.plusDays(5).atTime(8, 0),  15, 150),
            new ClaseGrupal(8, "Yoga vespertino",     "Lic. Andrea Lopez",
                    base.plusDays(6).atTime(20, 0), 18, 150)
        ));
    }

    /** Genera 12 equipos del inventario. */
    public static List<Equipo> generarEquipos() {
        List<Equipo> datos = new ArrayList<>(List.of(
            new Equipo(101, "Caminadora Life Fitness #1",   "Cardio",
                    LocalDate.of(2023, 1, 15)),
            new Equipo(102, "Caminadora Life Fitness #2",   "Cardio",
                    LocalDate.of(2023, 1, 15)),
            new Equipo(103, "Eliptica Precor #1",            "Cardio",
                    LocalDate.of(2022, 6, 20)),
            new Equipo(104, "Bicicleta estatica Schwinn",    "Cardio",
                    LocalDate.of(2024, 3, 10)),
            new Equipo(105, "Remo Concept2",                 "Cardio",
                    LocalDate.of(2023, 9, 5)),
            new Equipo(106, "Banca olimpica con barra",      "Fuerza",
                    LocalDate.of(2021, 12, 1)),
            new Equipo(107, "Rack de mancuernas 5-50kg",     "Fuerza",
                    LocalDate.of(2022, 4, 18)),
            new Equipo(108, "Maquina de poleas",             "Fuerza",
                    LocalDate.of(2023, 2, 28)),
            new Equipo(109, "Prensa de piernas",             "Fuerza",
                    LocalDate.of(2023, 7, 14)),
            new Equipo(110, "Set de TRX",                    "Funcional",
                    LocalDate.of(2024, 5, 22)),
            new Equipo(111, "Cuerdas de batalla",            "Funcional",
                    LocalDate.of(2024, 8, 11)),
            new Equipo(112, "Kettlebells set",               "Funcional",
                    LocalDate.of(2023, 11, 30))
        ));

        // Marcar uno en reparacion para variedad
        datos.get(2).cambiarEstado(Equipo.Estado.EN_REPARACION);  // Eliptica #1
        return datos;
    }
}
