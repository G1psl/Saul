package controlador;

import modelo.Alumno;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import javax.persistence.*;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ManagedBean
@ViewScoped
public class AlumnoBean implements Serializable {

    private List<Alumno> lista = new ArrayList<>();
    private Part archivo;

    private EntityManagerFactory emf;
    private EntityManager em;

    // Constructor para inicializar conexión y cargar lista
    public AlumnoBean() {
        try {
            emf = Persistence.createEntityManagerFactory("AlumnoPU");
            em = emf.createEntityManager();
            cargar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cargar() {
        lista = em.createQuery("SELECT a FROM Alumno a", Alumno.class).getResultList();
    }

    public void guardarDesdeExcel() {
        try (InputStream is = archivo.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet hoja = workbook.getSheetAt(0);
            Iterator<Row> filas = hoja.iterator();

            if (filas.hasNext()) {
                filas.next(); // omitir encabezado
            }

            em.getTransaction().begin();

            while (filas.hasNext()) {
                Row fila = filas.next();
                Alumno a = new Alumno();
                a.setNombre(fila.getCell(0).getStringCellValue());
                a.setCorreo(fila.getCell(1).getStringCellValue());
                a.setCarrera(fila.getCell(2).getStringCellValue());
                em.persist(a);
            }

            em.getTransaction().commit();
            cargar();

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Carga exitosa", "Los alumnos fueron cargados desde el archivo Excel."));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error al cargar Excel", e.getMessage()));
            e.printStackTrace();
        }
    }

    // Getters y Setters
    public List<Alumno> getLista() {
        return lista;
    }

    public Part getArchivo() {
        return archivo;
    }

    public void setArchivo(Part archivo) {
        this.archivo = archivo;
    }
}
