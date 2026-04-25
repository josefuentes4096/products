import { useParams, Link, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import client from "../api/client";
import { useCarrito } from "../contexts/CarritoContext";
import { useAuth } from "../contexts/AuthContext";

function ProductDetail() {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { agregarProducto } = useCarrito();
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    setLoading(true);
    setError(null);
    client.get(`/products/${id}`)
      .then((res) => setProduct(res.data))
      .catch(() => setError("No se pudo cargar el producto."))
      .finally(() => setLoading(false));
  }, [id]);

  const handleAgregar = () => {
    if (user) {
      agregarProducto(product);
      navigate("/cart");
    } else {
      navigate("/login");
    }
  };

  if (loading)
    return (
      <div className="text-center py-5">
        <div className="spinner-border text-primary" role="status" />
        <div className="mt-2 text-muted">Cargando producto...</div>
      </div>
    );

  if (error)
    return <div className="alert alert-danger text-center my-4">{error}</div>;

  if (!product)
    return (
      <div className="text-center my-5">
        <h4 className="text-muted">Producto no encontrado.</h4>
        <Link to="/" className="btn btn-outline-primary mt-3">Volver al catálogo</Link>
      </div>
    );

  return (
    <div className="row">
      <div className="col-md-5 text-center">
        <img
          src={product.imageUrl}
          alt={product.name}
          className="img-fluid p-4"
          style={{ maxHeight: "400px", objectFit: "contain" }}
        />
      </div>
      <div className="col-md-7">
        <h1>{product.name}</h1>
        <p className="text-muted">{product.category}</p>
        <p className="lead">{product.description}</p>
        <h3 className="text-primary">${product.price}</h3>
        <p>Stock disponible: <strong>{product.stock}</strong></p>
        <button className="btn btn-success mt-3" onClick={handleAgregar}>
          Agregar al carrito
        </button>
      </div>
    </div>
  );
}

export default ProductDetail;
