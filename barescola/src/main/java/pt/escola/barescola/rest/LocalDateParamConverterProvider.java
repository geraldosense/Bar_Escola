package pt.escola.barescola.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

/** Permite usar {@code @QueryParam LocalDate} no formato ISO (aaaa-mm-dd). */
@Provider
public class LocalDateParamConverterProvider implements ParamConverterProvider {

    @Override
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(Class<T> tipo, Type generico, Annotation[] anotacoes) {
        if (tipo != LocalDate.class) {
            return null;
        }
        return (ParamConverter<T>) new ParamConverter<LocalDate>() {
            @Override
            public LocalDate fromString(String valor) {
                if (valor == null || valor.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(valor);
                } catch (DateTimeParseException e) {
                    throw new BadRequestException("Data inválida (use aaaa-mm-dd): " + valor);
                }
            }

            @Override
            public String toString(LocalDate valor) {
                return valor == null ? null : valor.toString();
            }
        };
    }
}
