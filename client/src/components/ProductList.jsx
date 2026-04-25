import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import ProductCard from "./ProductCard";
import client from "../api/client";

function ProductList({ category, modo = "publico" }) {
  const [productos, setProductos] = useState([]);
  const [error, setError] = useState(null);
  const [cargando, setCargando] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const url = category
      ? `/products/category/${category}`
      : "/products";

    client.get(url)
      .then((res) => {
        setProductos(res.data.content);
        setError(null);
      })
      .catch(() => setError("Error al cargar productos"))
      .finally(() => setCargando(false));
  }, [category]);

  const handleEliminar = (id) => {
    if (!window.confirm("¿Seguro que desea eliminar este producto?")) return;
    client.delete(`/products/${id}`)
      .then(() => setProductos(productos.filter((prod) => prod.id !== id)))
      .catch(() => setError("Error al eliminar"));
  };

  const handleEditar = (producto) => {
    navigate(`/admin/editarProducto/${producto.id}`);
  };

  if (cargando) return <p>Cargando productos...</p>;
  if (error) return <p className="text-danger">Error: {error}</p>;
  if (!productos.length) return <p>No hay productos disponibles.</p>;

  return (
    <div className="row g-4">
      {productos.map((producto) => (
        <div key={producto.id} className="col-sm-6 col-md-4 col-lg-3">
          <ProductCard
            producto={producto}
            modo={modo}
            onDelete={handleEliminar}
            onEdit={handleEditar}
          />
        </div>
      ))}
    </div>
  );
}

export default ProductList;
