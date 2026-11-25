import java.sql.*;
import java.nio.file.*;
import extra.config.*;
import extra.seguridad.*;
import dao.*;

public class CamposCifradosPrueba {
    static void check(boolean ok,String mensaje){if(!ok)throw new AssertionError(mensaje);}
    static int id(Connection c,String sql)throws SQLException{try(var s=c.createStatement();var r=s.executeQuery(sql)){r.next();return r.getInt(1);}}
    public static void main(String[] args)throws Exception {
        String password="Prueba-local-2026";
        try(var c=DriverManager.getConnection(System.getenv("PAPELERIA_DB_URL"),"postgres","solo-cluster-temporal")) {
            int rol=id(c,"INSERT INTO roles(nombre) VALUES ('Prueba') RETURNING id_rol");
            int suc=id(c,"INSERT INTO sucursales(nombre) VALUES ('Prueba') RETURNING id_sucursal");
            try(var st=c.createStatement()) {
                for(String col:new String[]{"username","nombres","apellido_paterno","apellido_materno","password_hash"})st.execute("ALTER TABLE usuarios ALTER COLUMN "+col+" TYPE text");
                for(String col:new String[]{"telefono","correo","direccion"})st.execute("ALTER TABLE proveedores ALTER COLUMN "+col+" TYPE text");
                st.execute("ALTER TABLE usuarios ADD COLUMN username_busqueda varchar(64) NOT NULL UNIQUE");
                st.execute("CREATE TABLE seguridad_cifrado(id integer PRIMARY KEY,comprobante text)");
            }
            try(var ps=c.prepareStatement("INSERT INTO seguridad_cifrado VALUES (1,?)")){ps.setString(1,CifradoDatos.cifrar("papeleria-campos-v1","configuracion"));ps.executeUpdate();}
            CifradoDatos.comprobar(c);
            int usuario=new AdministracionDAO().crearUsuario(suc,rol,"usuario_prueba",password,"José","Pérez",null);
            // Verifica que usuarios antiguos conservan el acceso y pasan a AES al autenticarse.
            String hash;
            try(var st=c.createStatement();var rs=st.executeQuery("SELECT crypt('"+password+"',gen_salt('bf',12))")){rs.next();hash=rs.getString(1);}
            try(var ps=c.prepareStatement("UPDATE usuarios SET password_hash=? WHERE id_usuario=?")){ps.setString(1,CifradoDatos.protegerHashAnterior(hash));ps.setInt(2,usuario);ps.executeUpdate();}
            int proveedor=new CatalogoDAO().crearProveedor("Proveedor prueba",null,null,null,"555123","correo@prueba.invalid","Calle de prueba");
            try(var st=c.createStatement()){st.execute(Files.readString(Path.of(args[0])));}
            try(var s=c.createStatement();var rs=s.executeQuery("SELECT username,nombres,password_hash,username_busqueda FROM usuarios WHERE id_usuario="+usuario)){
                rs.next();check(rs.getString(1).startsWith("ecb1:")&&!rs.getString(1).contains("usuario_prueba"),"Username legible");
                check(rs.getString(2).startsWith("ecb1:"),"Nombre legible");check(CifradoDatos.esLegado(rs.getString(3)),"Hash cambiado");
                check(rs.getString(4).equals(CifradoDatos.indiceUsuario("usuario_prueba")),"Indice incorrecto");
            }
            try(var s=c.createStatement();var rs=s.executeQuery("SELECT username FROM usuarios_hist")){while(rs.next())check(rs.getString(1).startsWith("ecb1:"),"Historial sin cifrar");}
            UsuarioDAO login=new UsuarioDAO();
            check(login.autenticar("usuario_prueba",password).getNombres().equals("José"),"Login migrado");
            try(var s=c.createStatement();var rs=s.executeQuery("SELECT password_hash FROM usuarios WHERE id_usuario="+usuario)) {rs.next();check(!CifradoDatos.esLegado(rs.getString(1)),"No convirtió contraseña al login");check(password.equals(CifradoDatos.descifrar(rs.getString(1),"usuarios.password")),"Contraseña AES no recuperable");}
            check(login.autenticar("usuario_prueba","incorrecta")==null,"Contraseña incorrecta aceptada");
            check(login.autenticar("inexistente",password)==null,"Usuario inexistente aceptado");
            AdministracionDAO admin=new AdministracionDAO();
            check(admin.usuarios(suc,"pérez").size()==1,"Busqueda nombres");
            int nuevo=admin.crearUsuario(suc,rol,"nuevo_prueba",password,"Ana","López",null);
            check(login.existeUsuarioActivo("nuevo_prueba"),"Busqueda HMAC");
            try{admin.crearUsuario(suc,rol,"nuevo_prueba",password,"Otro","Otro",null);throw new AssertionError("Username duplicado");}
            catch(SQLException expected){check("23505".equals(expected.getSQLState()),"Fallo inesperado");}
            UsuarioActual.establecer("usuario_prueba");
            admin.actualizarUsuario(suc,nuevo,rol,"Ana María","López","Ruiz");
            check(admin.usuario(suc,nuevo).get("nombres").equals("Ana María"),"Actualizacion usuario");
            admin.cambiarContrasena(suc,nuevo,"Otra-clave-2026");
            check(login.autenticar("nuevo_prueba","Otra-clave-2026")!=null,"Cambio contraseña");
            try(var s=c.createStatement();var rs=s.executeQuery("SELECT hist_usuario_app FROM usuarios_hist WHERE hist_usuario_app IS NOT NULL")){
                check(rs.next(),"Falta auditoria");check("usuario_prueba".equals(CifradoDatos.descifrar(rs.getString(1),"auditoria.hist_usuario_app")),"Auditoria legible/incorrecta");
            }
            CatalogoDAO catalogo=new CatalogoDAO();
            check(catalogo.proveedores("correo@prueba").size()==1,"Busqueda correo");
            check(catalogo.proveedor(proveedor).get("telefono").equals("555123"),"Telefono migrado");
            int p=catalogo.crearProveedor("Otro",null,null,null,null,"nuevo@prueba.invalid","Dirección á");
            check(catalogo.proveedor(p).get("telefono")==null,"NULL perdido");
            catalogo.actualizarProveedor(p,"Otro",null,null,null,"999",null,"");
            check(catalogo.proveedor(p).get("direccion").equals(""),"Cadena vacia perdida");
            // Triggers siguen activos y copia de datos nuevos también cifrada.
            try(var s=c.createStatement();var rs=s.executeQuery("SELECT correo,direccion FROM proveedores_hist WHERE id_proveedor="+p)){
                check(rs.next(),"Historial desactivado");check(rs.getString(1).startsWith("ecb1:"),"Correo legible en historial");
            }
            System.out.println("OK: login, búsquedas, unicidad, creación/edición, transición BCrypt a AES-128, NULL e historial cifrado.");
        } finally {UsuarioActual.limpiar();ConexionBD.cerrarPool();}
    }
}
