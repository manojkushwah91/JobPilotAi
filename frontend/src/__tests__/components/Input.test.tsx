import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Input } from '@/components/ui/input';

describe('Input', () => {
  it('renders and accepts input', async () => {
    const user = userEvent.setup();
    render(<Input placeholder="Enter name" />);
    const input = screen.getByPlaceholderText('Enter name');
    await user.type(input, 'John Doe');
    expect(input).toHaveValue('John Doe');
  });

  it('disables input', () => {
    render(<Input disabled />);
    expect(screen.getByRole('textbox')).toBeDisabled();
  });
});
