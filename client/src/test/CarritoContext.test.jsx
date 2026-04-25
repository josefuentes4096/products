import { render, screen, act, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { vi } from 'vitest'
import { CarritoProvider, useCarrito } from '../contexts/CarritoContext'

// Mock del cliente axios
vi.mock('../api/client', () => ({
  default: {
    post: vi.fn(),
  },
}))

import client from '../api/client'

// Componente auxiliar para exponer el contexto en los tests
function CarritoConsumer({ onRender }) {
  const carrito = useCarrito()
  onRender(carrito)
  return null
}

function renderCarrito(onRender) {
  return render(
    <CarritoProvider>
      <CarritoConsumer onRender={onRender} />
    </CarritoProvider>
  )
}

const guitarra = { id: 1, name: 'Fender Stratocaster', price: 1500, imageUrl: '', category: 'Guitarras', stock: 8 }
const pedal    = { id: 2, name: 'Boss DS-1',           price: 80,   imageUrl: '', category: 'Pedales',   stock: 20 }

// -------------------------------------------------------------------------
// agregarProducto
// -------------------------------------------------------------------------

test('agregarProducto agrega un producto nuevo con cantidad 1', () => {
  let carrito
  renderCarrito((c) => { carrito = c })

  act(() => carrito.agregarProducto(guitarra))

  expect(carrito.productos).toHaveLength(1)
  expect(carrito.productos[0].cantidad).toBe(1)
  expect(carrito.productos[0].name).toBe('Fender Stratocaster')
})

test('agregarProducto incrementa cantidad si el producto ya existe', () => {
  let carrito
  renderCarrito((c) => { carrito = c })

  act(() => carrito.agregarProducto(guitarra))
  act(() => carrito.agregarProducto(guitarra))

  expect(carrito.productos).toHaveLength(1)
  expect(carrito.productos[0].cantidad).toBe(2)
})

test('agregarProducto agrega productos distintos como items separados', () => {
  let carrito
  renderCarrito((c) => { carrito = c })

  act(() => carrito.agregarProducto(guitarra))
  act(() => carrito.agregarProducto(pedal))

  expect(carrito.productos).toHaveLength(2)
})

// -------------------------------------------------------------------------
// disminuirProducto
// -------------------------------------------------------------------------

test('disminuirProducto resta 1 a la cantidad del producto', () => {
  let carrito
  renderCarrito((c) => { carrito = c })

  act(() => carrito.agregarProducto(guitarra))
  act(() => carrito.agregarProducto(guitarra))
  act(() => carrito.disminuirProducto(guitarra.id))

  expect(carrito.productos[0].cantidad).toBe(1)
})

test('disminuirProducto elimina el producto si la cantidad llega a 0', () => {
  let carrito
  renderCarrito((c) => { carrito = c })

  act(() => carrito.agregarProducto(guitarra))
  act(() => carrito.disminuirProducto(guitarra.id))

  expect(carrito.productos).toHaveLength(0)
})

// -------------------------------------------------------------------------
// eliminarProducto
// -------------------------------------------------------------------------

test('eliminarProducto quita el producto del carrito independientemente de la cantidad', () => {
  let carrito
  renderCarrito((c) => { carrito = c })

  act(() => carrito.agregarProducto(guitarra))
  act(() => carrito.agregarProducto(guitarra))
  act(() => carrito.agregarProducto(guitarra))
  act(() => carrito.eliminarProducto(guitarra.id))

  expect(carrito.productos).toHaveLength(0)
})

// -------------------------------------------------------------------------
// vaciarCarrito
// -------------------------------------------------------------------------

test('vaciarCarrito elimina todos los productos', () => {
  let carrito
  renderCarrito((c) => { carrito = c })

  act(() => carrito.agregarProducto(guitarra))
  act(() => carrito.agregarProducto(pedal))
  act(() => carrito.vaciarCarrito())

  expect(carrito.productos).toHaveLength(0)
})

// -------------------------------------------------------------------------
// confirmarPedido
// -------------------------------------------------------------------------

test('confirmarPedido llama a POST /orders con los items correctos', async () => {
  client.post.mockResolvedValueOnce({ data: { orderId: 1 } })

  let carrito
  renderCarrito((c) => { carrito = c })

  act(() => carrito.agregarProducto(guitarra))
  act(() => carrito.agregarProducto(pedal))
  await act(() => carrito.confirmarPedido(7))

  expect(client.post).toHaveBeenCalledWith('/orders', {
    userId: 7,
    items: [
      { productId: 1, quantity: 1 },
      { productId: 2, quantity: 1 },
    ],
  })
})

test('confirmarPedido vacía el carrito al tener éxito', async () => {
  client.post.mockResolvedValueOnce({ data: { orderId: 1 } })

  let carrito
  renderCarrito((c) => { carrito = c })

  act(() => carrito.agregarProducto(guitarra))
  await act(() => carrito.confirmarPedido())

  expect(carrito.productos).toHaveLength(0)
})

test('confirmarPedido setea pedidoExitoso en true al confirmar', async () => {
  client.post.mockResolvedValueOnce({ data: { orderId: 1 } })

  let carrito
  renderCarrito((c) => { carrito = c })

  act(() => carrito.agregarProducto(guitarra))
  await act(() => carrito.confirmarPedido())

  expect(carrito.pedidoExitoso).toBe(true)
})

test('confirmarPedido setea pedidoError si la API falla', async () => {
  client.post.mockRejectedValueOnce(new Error('Network error'))

  let carrito
  renderCarrito((c) => { carrito = c })

  act(() => carrito.agregarProducto(guitarra))
  await act(() => carrito.confirmarPedido())

  expect(carrito.pedidoError).toBeTruthy()
  expect(carrito.productos).toHaveLength(1) // no vacía el carrito
})
