import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import client from "../api/client";

export default function FormularioEdicion() {
  const [producto, setProducto] = useState(null);
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(true);
  const navigate = useNavigate();
  const { id } = useParams();

  useEffect(() => {
    client.get(`/products/${id}`)
      .then((res) => {
        setProducto(res.data);
        setCargando(false);
      })
      .catch(() => {
        setError("No se pudo cargar el producto.");
        setCargando(false);
      });
  }, [id]);

  const handleChange = (e) => {
    setProducto({ ...producto, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!producto.name.trim() || producto.description.length < 10 || parseFloat(producto.price) <= 0) {
      return setError("Complete todos los campos correctamente.");
    }

    setCargando(true);
    client.put(`/products/${id}`, {
      name: producto.name,
      price: parseFloat(producto.price),
      description: producto.description,
      imageUrl: producto.imageUrl,
      category: producto.category,
      stock: parseInt(producto.stock),
    })
      .then(() => navigate("/admin"))
      .catch(() => setError("Error al actualizar el producto."))
      .finally(() => setCargando(false));
  };

  if (cargando) return <p className="text-center mt-5">Cargando producto...</p>;

  return (
    <div className="container mt-5">
      <h2>Editar Producto</h2>
      {error && <div className="alert alert-danger">{error}</div>}
      {producto && (
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label>Nombre</label>
            <input name="name" className="form-control" value={producto.name} onChange={handleChange} />
          </div>
          <div className="mb-3">
            <label>Precio</label>
            <input name="price" type="number" className="form-control" value={producto.price} onChange={handleChange} />
          </div>
          <div className="mb-3">
            <label>Descripción</label>
            <textarea name="description" className="form-control" value={producto.description} onChange={handleChange} />
          </div>
          <div className="mb-3">
            <label>Categoría</label>
            <input name="category" className="form-control" value={producto.category} onChange={handleChange} />
          </div>
          <div className="mb-3">
            <label>Stock</label>
            <input name="stock" type="number" className="form-control" value={producto.stock} onChange={handleChange} />
          </div>
          <div className="mb-3">
            <label>URL Imagen</label>
            <input name="imageUrl" className="form-control" value={producto.imageUrl ?? ""} onChange={handleChange} />
          </div>
          <button type="submit" className="btn btn-success" disabled={cargando}>
            {cargando ? "Guardando..." : "Guardar Cambios"}
          </button>
        </form>
      )}
    </div>
  );
}
