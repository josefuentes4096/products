import { render, screen, waitFor } from '@testing-library/react'
import { vi } from 'vitest'
import HistorialPedidos from '../pages/HistorialPedidos'

vi.mock('../api/client', () => ({
  default: { get: vi.fn() },
}))

import client from '../api/client'

beforeEach(() => vi.clearAllMocks())

const pedidos = [
  {
    orderId: 10,
    status: 'PENDING',
    total: 1580,
    items: [
      { name: 'Fender Stratocaster', quantity: 1, subtotal: 1500 },
      { name: 'Boss DS-1',           quantity: 1, subtotal: 80   },
    ],
  },
  {
    orderId: 11,
    status: 'DELIVERED',
    total: 200,
    items: [
      { name: 'Marshall DSL40', quantity: 1, subtotal: 200 },
    ],
  },
]

// -------------------------------------------------------------------------
// Lista de pedidos
// -------------------------------------------------------------------------

test('muestra los números de pedido y el estado en español', async () => {
  client.get.mockResolvedValueOnce({ data: pedidos })

  render(<HistorialPedidos />)

  await waitFor(() => {
    expect(screen.getByText('Pedido #10')).toBeInTheDocument()
    expect(screen.getByText('Pedido #11')).toBeInTheDocument()
    expect(screen.getByText('Pendiente')).toBeInTheDocument()
    expect(screen.getByText('Entregado')).toBeInTheDocument()
  })
})

test('muestra los items del pedido con nombre y cantidad', async () => {
  client.get.mockResolvedValueOnce({ data: pedidos })

  render(<HistorialPedidos />)

  await waitFor(() => {
    expect(screen.getByText('Fender Stratocaster')).toBeInTheDocument()
    expect(screen.getByText('Boss DS-1')).toBeInTheDocument()
  })
})

test('muestra el total del pedido formateado', async () => {
  client.get.mockResolvedValueOnce({ data: pedidos })

  render(<HistorialPedidos />)

  await waitFor(() => {
    expect(screen.getByText(/Total: \$1580\.00/)).toBeInTheDocument()
  })
})

// -------------------------------------------------------------------------
// Estado vacío
// -------------------------------------------------------------------------

test('muestra mensaje cuando no hay pedidos registrados', async () => {
  client.get.mockResolvedValueOnce({ data: [] })

  render(<HistorialPedidos />)

  await waitFor(() => {
    expect(screen.getByText(/no hay pedidos registrados/i)).toBeInTheDocument()
  })
})

// -------------------------------------------------------------------------
// Error de API
// -------------------------------------------------------------------------

test('muestra error si no se puede cargar el historial', async () => {
  client.get.mockRejectedValueOnce(new Error('Network error'))

  render(<HistorialPedidos />)

  await waitFor(() => {
    expect(screen.getByText(/no se pudo cargar el historial/i)).toBeInTheDocument()
  })
})
