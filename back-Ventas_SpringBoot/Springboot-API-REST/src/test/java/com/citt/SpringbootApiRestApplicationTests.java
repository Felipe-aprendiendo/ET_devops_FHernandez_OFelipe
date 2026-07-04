package com.citt;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Requiere conexión real a BD vía variables de entorno DB_ENDPOINT/etc., no disponibles
// en el runner de CI. Ver VentaServiceTest para cobertura real de lógica de negocio.
@Disabled("Requiere conexión real a BD vía variables de entorno DB_ENDPOINT/etc., no disponibles en el runner de CI. Ver VentaServiceTest para cobertura real de lógica de negocio.")
@SpringBootTest
class SpringbootApiRestApplicationTests {

	@Test
	void contextLoads() {
	}

}
