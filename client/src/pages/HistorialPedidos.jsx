import { useEffect, useState } from "react";
import client from "../api/client";

const ESTADO_BADGE = {
  PENDING:   "warning",
  CONFIRMED: "info",
  SHIPPED:   "primary",
  DELIVERED: "success",
  CANCELLED: "danger",
};

const ESTADO_LABEL = {
  PENDING:   "Pendiente",
  CONFIRMED: "Confirmado",
  SHIPPED:   "Enviado",
  DELIVERED: "Entregado",
  CANCELLED: "Cancelado",
};

export default function HistorialPedidos() {
  const [pedidos, setPedidos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    client.get("/orders/user/1")
      .then((res) => setPedidos(res.data))
      .catch(() => setError("No se pudo cargar el historial."))
      .finally(() => setCargando(false));
  }, []);

  if (cargando) return <p className="text-center mt-5">Cargando historial...</p>;
  if (error) return <div className="alert alert-danger">{error}</div>;

  return (
    <div className="container py-4">
      <h4 className="mb-4">Historial de Pedidos</h4>

      {pedidos.length === 0 ? (
        <p className="text-muted">No hay pedidos registrados.</p>
      ) : (
        pedidos.map((pedido) => (
          <div key={pedido.orderId} className="card mb-3 shadow-sm">
            <div className="card-header d-flex justify-content-between align-items-center">
              <span className="fw-bold">Pedido #{pedido.orderId}</span>
              <span className={`badge bg-${ESTADO_BADGE[pedido.status] || "secondary"}`}>
                {ESTADO_LABEL[pedido.status] || pedido.status}
              </span>
            </div>
            <div className="card-body">
              {pedido.items && pedido.items.length > 0 && (
                <table className="table table-sm mb-2">
                  <thead className="table-light">
                    <tr>
                      <th>Producto</th>
                      <th>Cantidad</th>
                      <th className="text-end">Subtotal</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pedido.items.map((item, i) => (
                      <tr key={i}>
                        <td>{item.name}</td>
                        <td>{item.quantity}</td>
                        <td className="text-end">${item.subtotal?.toFixed(2)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
              <p className="mb-0 fw-bold text-end text-primary">
                Total: ${pedido.total?.toFixed(2)}
              </p>
            </div>
          </div>
        ))
      )}
    </div>
  );
}
