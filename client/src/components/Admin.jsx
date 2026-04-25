import { Link } from "react-router-dom";
import ProductList from "../components/ProductList";

function Admin() {
  return (
    <div className="container">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h4>Gestión de Productos</h4>
        <Link to="/admin/agregarProducto" className="btn btn-success btn-sm">+ Agregar Producto</Link>
      </div>
      <ProductList modo="admin" />
    </div>
  );
}

export default Admin;
