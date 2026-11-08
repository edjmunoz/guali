package edu.umg.desarrolloweb.guali.web.servicio;

import edu.umg.desarrolloweb.guali.web.dto.PreferenciaDto;
import edu.umg.desarrolloweb.guali.web.modelo.Preferencia;
import edu.umg.desarrolloweb.guali.web.repositorio.PreferenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreferenciaService {

    private final PreferenciaRepository repositorio;

    public PreferenciaService(PreferenciaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public PreferenciaDto obtener(String clienteId) {
        Preferencia p = repositorio.findById(clienteId).orElseGet(() -> new Preferencia(clienteId));
        return new PreferenciaDto(p.getTamanoFuente(), p.getTema());
    }

    @Transactional
    public PreferenciaDto guardar(String clienteId, PreferenciaDto dto) {
        Preferencia p = repositorio.findById(clienteId).orElseGet(() -> new Preferencia(clienteId));
        p.setTamanoFuente(dto.tamanoFuente());
        p.setTema(dto.tema() == null ? "claro" : dto.tema());
        repositorio.save(p);
        return new PreferenciaDto(p.getTamanoFuente(), p.getTema());
    }
}
