package exa15;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author oracle
 */
public class Exa15 {

    public static Connection conexion = null;
    public static ObjectInputStream ois;
    public static XMLOutputFactory xof;
    public static XMLStreamWriter xsw;
    public static Platos plato;

    public static Connection getConexion() throws SQLException {
        String usuario = "hr";
        String password = "hr";
        String host = "localhost";
        String puerto = "1521";
        String sid = "orcl";
        String ulrjdbc = "jdbc:oracle:thin:" + usuario + "/" + password + "@" + host + ":" + puerto + ":" + sid;

        conexion = DriverManager.getConnection(ulrjdbc);
        return conexion;
    }

    public static void closeConexion() throws SQLException {
        conexion.close();
    }

    //NO SE USA, VA TODO EN EL MAIN
    public static int calcularGraxasTotais(String codp) throws SQLException {
        String sql = "select codc,peso from composicion where codp='" + codp + "'";

        String codc = "";
        int peso = 0, total = 0;

        Statement stmt = conexion.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            codc = rs.getString("codc");
            peso = rs.getInt("peso");
            String sql2 = "select graxa from componentes where codc='" + codc + "'";
            Statement stmt2 = conexion.createStatement();
            ResultSet rs2 = stmt2.executeQuery(sql2);
            rs2.next(); //si hay mas de un resultado bucle while
            int graxa = rs2.getInt("graxa");
            int graxa_parcial = (graxa * peso) / 100;
            System.out.println("codigo do componente: " + codc + "-> graxa por cada 100 gr= " + graxa);
            System.out.println("peso: " + peso);

            total = total + graxa_parcial;
            System.out.println("total de graxa do componente: " + graxa_parcial);
//            System.out.println(total);
        }
        System.out.println("\nTOTAL EN GRAXAS DO PLATO: " + total + "\n");

        return total;

//        System.out.println(total);
    }

    public static void main(String[] args) throws XMLStreamException, IOException, ClassNotFoundException, SQLException {

        Exa15.getConexion();
        ois = new ObjectInputStream(new FileInputStream("platoss"));
        xof = XMLOutputFactory.newInstance();
        xsw = xof.createXMLStreamWriter(new FileWriter("totalgraxas.xml"));

        xsw.writeStartDocument("1.0");
        xsw.writeStartElement("Platos");

        while ((plato = (Platos) ois.readObject()) != null) {
            xsw.writeStartElement("Plato");
            System.out.println("CODIGO DO PLATO: " + plato.getCodigop());
            xsw.writeAttribute("Codigop", plato.getCodigop());
            System.out.println("nome do plato: " + plato.getNomep());
            xsw.writeStartElement("nomep");
            xsw.writeCharacters(plato.getNomep());
            xsw.writeEndElement();
            xsw.writeStartElement("graxatotal");
            String sql = "select codc,peso from composicion where codp='" + plato.getCodigop() + "'";

            String codc = "";
            int peso = 0, total = 0;

            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                codc = rs.getString("codc");
                peso = rs.getInt("peso");
                String sql2 = "select graxa from componentes where codc='" + codc + "'";
                Statement stmt2 = conexion.createStatement();
                ResultSet rs2 = stmt2.executeQuery(sql2);
                rs2.next(); //si hay mas de un resultado bucle while
                int graxa = rs2.getInt("graxa");
                System.out.println("codigo do componente: " + codc + "-> graxa por cada 100 gr= " + graxa);
                System.out.println("peso: " + peso);
                int graxa_parcial = (graxa * peso) / 100;
                total = total + graxa_parcial;
                System.out.println("total de graxa do componente: " + graxa_parcial);
            }
            System.out.println("\nTOTAL EN GRAXAS DO PLATO: " + total + "\n");
            
            xsw.writeCharacters(String.valueOf(total));
//se activaria esta linea si se usara el metodo calcularGraxasTotais()
//            xsw.writeCharacters(String.valueOf(Exa15.calcularGraxasTotais(plato.getCodigop())));
            xsw.writeEndElement();
            xsw.writeEndElement();

        }

        xsw.writeEndElement();
//        xsw.writeEndDocument(); //valen igual ambos
        ois.close();
        //xsw.flush();
        xsw.close();

        Exa15.closeConexion();
    }
}
