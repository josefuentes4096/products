import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import client from "../api/client";

export default function BuscarProducto() {
  const [searchParams] = useSearchParams();
  const [modo, setModo] = useState(searchParams.get("modo") === "id" ? "id" : "nombre");
  const [query, setQuery] = useState("");
  const [producto, setProducto] = useState(null);
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);

  const buscar = async (e) => {
    e.preventDefault();
    if (!query.trim()) return setError("Ingresá un valor para buscar.");
    setCargando(true);
    setError("");
    setProducto(null);
    try {
      const url = modo === "id"
        ? `/products/${query}`
        : `/products/search?name=${encodeURIComponent(query)}`;
      const res = await client.get(url);
      setProducto(res.data);
    } catch {
      setError("Producto no encontrado.");
    } finally {
      setCargando(false);
    }
  };

  const cambiarModo = (nuevoModo) => {
    setModo(nuevoModo);
    setProducto(null);
    setError("");
    setQuery("");
  };

  return (
    <div className="container py-4">
      <h4 className="mb-4">Buscar Producto</h4>

      <div className="btn-group mb-3">
        <button
          className={`btn ${modo === "id" ? "btn-primary" : "btn-outline-primary"}`}
          onClick={() => cambiarModo("id")}
        >
          Por ID
        </button>
        <button
          className={`btn ${modo === "nombre" ? "btn-primary" : "btn-outline-primary"}`}
          onClick={() => cambiarModo("nombre")}
        >
          Por Nombre
        </button>
      </div>

      <form onSubmit={buscar} className="d-flex gap-2 mb-4">
        <input
          className="form-control"
          placeholder={modo === "id" ? "Ingresá el ID del producto" : "Ingresá el nombre exacto del producto"}
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <button type="submit" className="btn btn-primary" disabled={cargando}>
          {cargando ? "Buscando..." : "Buscar"}
        </button>
      </form>

      {error && <div className="alert alert-danger">{error}</div>}

      {producto && (
        <div className="card">
          <div className="card-body">
            <div className="row align-items-center">
              <div className="col-md-3 text-center">
                <img
                  src={producto.imageUrl}
                  alt={producto.name}
                  className="img-fluid rounded"
                  style={{ maxHeight: "150px", objectFit: "contain" }}
                />
              </div>
              <div className="col-md-9">
                <h5 className="card-title">{producto.name}</h5>
                <p className="text-muted mb-1">
                  <strong>ID:</strong> {producto.id} &nbsp;|&nbsp;
                  <strong>Categoría:</strong> {producto.category}
                </p>
                <p className="card-text">{producto.description}</p>
                <p className="fw-bold text-primary fs-5 mb-1">${producto.price}</p>
                <p className="mb-3">
                  Stock disponible:{" "}
                  <span className={`fw-bold ${producto.stock <= 3 ? "text-danger" : "text-success"}`}>
                    {producto.stock}
                  </span>
                  {producto.stock <= 3 && <span className="badge bg-danger ms-2">Stock bajo</span>}
                </p>
                <Link to={`/admin/editarProducto/${producto.id}`} className="btn btn-warning btn-sm">
                  Actualizar Producto
                </Link>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
