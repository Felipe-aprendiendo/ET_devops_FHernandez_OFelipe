package persistence.service;

import com.citt.exceptions.DespachoNotFoundException;
import com.citt.persistence.entity.Despacho;
import com.citt.persistence.repository.DespachoRepository;
import com.citt.persistence.services.DespachoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DespachoServiceTest {

    @Mock
    private DespachoRepository despachoRepository;

    @InjectMocks
    private DespachoServiceImpl despachoService;

    private Despacho despacho;

    @BeforeEach
    public void setUp(){
        despacho = new Despacho();
        despacho.setIdDespacho(1L);
        despacho.setFechaDespacho(LocalDate.of(2025,4,14));
        despacho.setPatenteCamion("ABCD12");
        despacho.setIntento(1);
        despacho.setIdCompra(1L);
        despacho.setDireccionCompra("Calle Falsa 123");
        despacho.setValorCompra(1000L);
        despacho.setDespachado(false);
    }

    @Test
    @DisplayName("Cuando existen despachos, entonces findAllDespachos retorna la lista completa")
    public void whenDespachosExist_thenFindAllReturnsList(){
        // Prepara la simulación
        when(despachoRepository.findAll()).thenReturn(Collections.singletonList(despacho));

        // Llama al servicio
        List<Despacho> result = despachoService.findAllDespachos();

        // Verifica el resultado
        verify(despachoRepository, times(1)).findAll();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(despacho.getIdDespacho(), result.get(0).getIdDespacho());
    }

    @Test
    @DisplayName("Cuando se guarda un despacho válido, entonces se persiste correctamente")
    public void whenSavingValidDespacho_thenItIsPersistedCorrectly(){
        // Prepara la simulación
        when(despachoRepository.save(any(Despacho.class))).thenReturn(despacho);

        // Llama al servicio
        Despacho savedDespacho = despachoService.saveDespacho(despacho);

        // Verifica el resultado
        verify(despachoRepository, times(1)).save(despacho);
        assertNotNull(savedDespacho);
        assertEquals(despacho.getDireccionCompra(), savedDespacho.getDireccionCompra());
        assertEquals(despacho.getPatenteCamion(), savedDespacho.getPatenteCamion());
        assertEquals(despacho.getValorCompra(), savedDespacho.getValorCompra());
        assertEquals(despacho.isDespachado(), savedDespacho.isDespachado());
    }

    @Test
    @DisplayName("Cuando el despacho existe, entonces updateDespacho actualiza y retorna el despacho")
    public void whenDespachoExists_thenUpdateDespachoUpdatesAndReturnsIt() throws DespachoNotFoundException {
        // Prepara los datos nuevos a aplicar sobre el despacho existente
        Despacho datosActualizados = new Despacho();
        datosActualizados.setFechaDespacho(LocalDate.of(2025,5,1));
        datosActualizados.setPatenteCamion("ZZZZ99");
        datosActualizados.setIntento(2);
        datosActualizados.setIdCompra(1L);
        datosActualizados.setDireccionCompra("Nueva Direccion 456");
        datosActualizados.setValorCompra(2000L);
        datosActualizados.setDespachado(true);

        when(despachoRepository.findById(1L)).thenReturn(Optional.of(despacho));
        when(despachoRepository.save(any(Despacho.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Llama al servicio
        Despacho result = despachoService.updateDespacho(1L, datosActualizados);

        // Verifica el resultado
        verify(despachoRepository, times(1)).save(despacho);
        assertNotNull(result);
        assertEquals(datosActualizados.getFechaDespacho(), result.getFechaDespacho());
        assertEquals(datosActualizados.getPatenteCamion(), result.getPatenteCamion());
        assertEquals(datosActualizados.getIntento(), result.getIntento());
        assertEquals(datosActualizados.getIdCompra(), result.getIdCompra());
        assertEquals(datosActualizados.getDireccionCompra(), result.getDireccionCompra());
        assertEquals(datosActualizados.getValorCompra(), result.getValorCompra());
        assertEquals(datosActualizados.isDespachado(), result.isDespachado());
    }

    @Test
    @DisplayName("Cuando el despacho no existe, entonces updateDespacho lanza DespachoNotFoundException")
    public void whenDespachoDoesNotExist_thenUpdateDespachoThrowsException(){
        // Prepara la simulación: no hay despacho con ese ID
        when(despachoRepository.findById(99L)).thenReturn(Optional.empty());

        // Llama al servicio y verifica que lanza la excepción esperada
        assertThrows(DespachoNotFoundException.class,
                () -> despachoService.updateDespacho(99L, despacho));

        // Verifica que nunca se intenta guardar
        verify(despachoRepository, never()).save(any(Despacho.class));
    }
}
