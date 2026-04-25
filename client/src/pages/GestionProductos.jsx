import { Link } from "react-router-dom";

export default function GestionProductos() {
  return (
    <div className="container py-4">
      <h2 className="text-center fw-bold mb-1">SISTEMA DE GESTIÓN - TECHLAB</h2>
      <h5 className="text-center text-secondary mb-4">——— Gestión de Productos ———</h5>
      <div className="row justify-content-center">
        <div className="col-md-6">
          <div className="list-group">
            <Link to="/admin/agregarProducto" className="list-group-item list-group-item-action list-group-item-primary">
              a) Agregar Producto
            </Link>
            <Link to="/" className="list-group-item list-group-item-action">
              b) Listar Productos
            </Link>
            <Link to="/buscar?modo=id" className="list-group-item list-group-item-action">
              c) Buscar Producto por ID
            </Link>
            <Link to="/buscar" className="list-group-item list-group-item-action">
              d) Buscar / Actualizar Producto por Nombre
            </Link>
            <Link to="/admin/productos" className="list-group-item list-group-item-action list-group-item-danger">
              e) Eliminar Producto
            </Link>
            <Link to="/" className="list-group-item list-group-item-action list-group-item-secondary">
              f) Volver al menú principal
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
