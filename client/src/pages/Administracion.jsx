import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import client from "../api/client";

export default function Administracion() {
  const [stockBajo, setStockBajo] = useState([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    client.get("/products/low-stock")
      .then((res) => setStockBajo(res.data.content))
      .catch(() => {})
      .finally(() => setCargando(false));
  }, []);

  return (
    <div className="container py-4">
      <h4 className="mb-4">Administración</h4>

      <div className="row g-4 mb-4">
        <div className="col-md-4">
          <div className="card h-100">
            <div className="card-header fw-bold">Gestión de Productos</div>
            <div className="card-body d-flex flex-column gap-2">
              <Link to="/admin/productos" className="btn btn-outline-primary btn-sm">Ver todos los productos</Link>
              <Link to="/admin/agregarProducto" className="btn btn-outline-success btn-sm">+ Agregar producto</Link>
              <Link to="/buscar" className="btn btn-outline-secondary btn-sm">Buscar producto</Link>
            </div>
          </div>
        </div>

        <div className="col-md-8">
          <div className="card h-100 border-warning">
            <div className="card-header bg-warning text-dark fw-bold">
              ⚠ Alertas de Stock Mínimo (≤ 5 unidades)
            </div>
            <div className="card-body">
              {cargando ? (
                <p className="text-muted">Cargando...</p>
              ) : stockBajo.length === 0 ? (
                <p className="text-success mb-0">✓ Todos los productos tienen stock suficiente.</p>
              ) : (
                <table className="table table-sm mb-0">
                  <thead>
                    <tr>
                      <th>Producto</th>
                      <th>Categoría</th>
                      <th>Stock</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {stockBajo.map((p) => (
                      <tr key={p.id}>
                        <td>{p.name}</td>
                        <td>{p.category}</td>
                        <td><span className="badge bg-danger">{p.stock}</span></td>
                        <td>
                          <Link to={`/admin/editarProducto/${p.id}`} className="btn btn-warning btn-sm">
                            Actualizar
                          </Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="card border-secondary">
        <div className="card-header text-muted">Gestión de Usuarios</div>
        <div className="card-body">
          <p className="text-muted mb-0">La gestión de usuarios no está disponible en esta versión.</p>
        </div>
      </div>
    </div>
  );
}
