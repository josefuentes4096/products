import { render, screen, waitFor } from '@testing-library/react'
import { vi } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import Administracion from '../pages/Administracion'

vi.mock('../api/client', () => ({
  default: { get: vi.fn() },
}))

import client from '../api/client'

beforeEach(() => vi.clearAllMocks())

const stockBajoProductos = [
  { id: 1, name: 'Fender Stratocaster', category: 'Guitarras', stock: 2 },
  { id: 2, name: 'Boss DS-1',           category: 'Pedales',   stock: 1 },
]

function renderAdmin() {
  return render(
    <MemoryRouter>
      <Administracion />
    </MemoryRouter>
  )
}

// -------------------------------------------------------------------------
// Alertas de stock mínimo
// -------------------------------------------------------------------------

test('muestra los productos con stock bajo en la tabla', async () => {
  client.get.mockResolvedValueOnce({ data: { content: stockBajoProductos } })

  renderAdmin()

  await waitFor(() => {
    expect(screen.getByText('Fender Stratocaster')).toBeInTheDocument()
    expect(screen.getByText('Boss DS-1')).toBeInTheDocument()
  })
})

test('muestra el stock como badge para cada producto', async () => {
  client.get.mockResolvedValueOnce({ data: { content: stockBajoProductos } })

  renderAdmin()

  await waitFor(() => {
    expect(screen.getByText('2')).toBeInTheDocument()
    expect(screen.getByText('1')).toBeInTheDocument()
  })
})

test('muestra mensaje cuando todos los productos tienen stock suficiente', async () => {
  client.get.mockResolvedValueOnce({ data: { content: [] } })

  renderAdmin()

  await waitFor(() => {
    expect(screen.getByText(/todos los productos tienen stock suficiente/i)).toBeInTheDocument()
  })
})

// -------------------------------------------------------------------------
// Links de gestión
// -------------------------------------------------------------------------

test('muestra los links de gestión de productos', async () => {
  client.get.mockResolvedValueOnce({ data: { content: [] } })

  renderAdmin()

  await waitFor(() => {
    expect(screen.getByRole('link', { name: /ver todos los productos/i })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /agregar producto/i })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /buscar producto/i })).toBeInTheDocument()
  })
})
