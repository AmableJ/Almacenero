package logica;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import logica.basesDatos.Conexion;

public class Gestor {

	private Conexion c = new Conexion();
	private Conexion c2 = new Conexion(); // conexion anidada
	private Conexion c3 = new Conexion(); // conexion anidada
	private ArrayList<Producto> productos;
	private ArrayList<Integer> idPedidos;
	private ArrayList<Pedido> pedidos;

	/**
	 * M�todo que lee de la base de datos
	 */
	private void cargarOrdenes() {
		c.crearConexion();
		c2.crearConexion();
		c3.crearConexion();
		try {
			PreparedStatement ps = c.getCon().prepareStatement(
					"SELECT * FROM _pedidos");
			ResultSet rs = ps.executeQuery();
			idPedidos = new ArrayList<Integer>();
			productos = new ArrayList<Producto>();
			while (rs.next()) {
				int id = Integer.parseInt(rs.getString("order_id"));
				if (!idPedidos.contains(id))
					idPedidos.add(id);
				int cantidad = Integer.parseInt(rs.getString("cantidad"));
				int idProducto = Integer.parseInt(rs.getString("product_id"));
				Context c = recogerCaracteristicas(idProducto);
				String posicion = recogerPosicion(idProducto);
				for (int i = 0; i < cantidad; i++) {
					// crear el objeto
					Producto p = new Producto(id,
							rs.getString("order_product_name"), posicion,
							rs.getString("order_product_code"), c.ancho,
							c.largo, c.alto, c.peso, Producto.POR_RECOGER);
					p.setCantidadTotalEnPedido(cantidad);
					productos.add(p);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		c3.cerraConexion();
		c2.cerraConexion();
		c.cerraConexion();
	}

	// clase auxiliar para evitar 3 llamadas a la bd
	private class Context {
		double peso;
		double largo;
		double ancho;
		double alto;
	}

	private Context recogerCaracteristicas(int idProducto) {
		Context c = new Context();
		try {
			PreparedStatement ps = c2
					.getCon()
					.prepareStatement(
							"SELECT * FROM `joomla_hikashop_product` WHERE `product_id`=?");
			ps.setInt(1, idProducto);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				c.peso = rs.getInt("product_weight");
				c.alto = rs.getInt("product_length");
				c.ancho = rs.getInt("product_width");
				c.largo = rs.getInt("product_height");
			}
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		}
		return c;
	}

	private String recogerPosicion(int idProducto) {
		String posicion = "";
		try {
			PreparedStatement ps = c3
					.getCon()
					.prepareStatement(
							"Select * from joomla_hikashop_warehouse, joomla_hikashop_product WHERE joomla_hikashop_warehouse.warehouse_id = joomla_hikashop_product.product_warehouse_id and joomla_hikashop_product.product_id=?");

			ps.setInt(1, idProducto);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				posicion = rs.getString("warehouse_name");
			}
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		}
		return posicion;
	}

	/**
	 * Genera los pedidos a partir de todas los productos este es uno de los
	 * prosibles m�todos de los que podr� leer la GUI
	 */
	public void generaPedidos() {
		cargarOrdenes();
		pedidos = new ArrayList<Pedido>(idPedidos.size());
		for (Integer idPedido : idPedidos) {
			Pedido p = new Pedido();
			p.setId(idPedido);
			for (Producto producto : productos) {
				if (producto.getOrder_id() == idPedido)
					p.a�adirProducto(producto);
			}
			pedidos.add(p);
		}
	}

	/*
	 * M�todo interno para comprobar pedidos
	 */
	public void compruebaPedidos() {
		for (Pedido pedido : pedidos) {
			pedido.muestra();
		}
	}

	/**
	 * Getters
	 */
	public ArrayList<Pedido> getPedidos() {
		return pedidos;
	}


	public void setPedidos(ArrayList<Pedido> pedidos) {
		this.pedidos = pedidos;
	}

}
