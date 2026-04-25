import { useState } from "react";
import { useNavigate } from "react-router-dom";
import client from "../api/client";

export default function FormularioProducto() {
  const [name, setName] = useState("");
  const [price, setPrice] = useState("");
  const [description, setDescription] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [category, setCategory] = useState("");
  const [stock, setStock] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);
  const navigate = useNavigate();

  const validar = () => {
    if (!name.trim()) return "El nombre es obligatorio.";
    if (parseFloat(price) <= 0) return "El precio debe ser mayor a 0.";
    if (description.length < 10) return "La descripción debe tener al menos 10 caracteres.";
    if (parseInt(stock) < 0) return "El stock no puede ser negativo.";
    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errorValidacion = validar();
    if (errorValidacion) return setError(errorValidacion);

    setCargando(true);
    setError("");

    try {
      await client.post("/products", {
        name,
        price: parseFloat(price),
        description,
        imageUrl,
        category,
        stock: parseInt(stock),
      });
      navigate("/admin");
    } catch {
      setError("Error al crear el producto. Intente nuevamente.");
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="container mt-5">
      <h2>Agregar Producto</h2>
      {error && <div className="alert alert-danger">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label htmlFor="name">Nombre</label>
          <input id="name" className="form-control" value={name} onChange={e => setName(e.target.value)} />
        </div>
        <div className="mb-3">
          <label htmlFor="price">Precio</label>
          <input id="price" type="number" className="form-control" value={price} onChange={e => setPrice(e.target.value)} />
        </div>
        <div className="mb-3">
          <label htmlFor="description">Descripción</label>
          <textarea id="description" className="form-control" value={description} onChange={e => setDescription(e.target.value)} />
        </div>
        <div className="mb-3">
          <label htmlFor="category">Categoría</label>
          <input id="category" className="form-control" value={category} onChange={e => setCategory(e.target.value)} />
        </div>
        <div className="mb-3">
          <label htmlFor="stock">Stock</label>
          <input id="stock" type="number" className="form-control" value={stock} onChange={e => setStock(e.target.value)} />
        </div>
        <div className="mb-3">
          <label htmlFor="imageUrl">URL de imagen (opcional)</label>
          <input id="imageUrl" className="form-control" value={imageUrl} onChange={e => setImageUrl(e.target.value)} />
        </div>
        <button type="submit" className="btn btn-primary" disabled={cargando}>
          {cargando ? "Guardando..." : "Guardar"}
        </button>
      </form>
    </div>
  );
}
